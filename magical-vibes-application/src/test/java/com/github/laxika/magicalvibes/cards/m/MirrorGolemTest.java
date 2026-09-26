package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.cards.g.GraniteShard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirrorGolem.class, CopperMyr.class, GraniteShard.class, FangrenHunter.class})
class MirrorGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability exiles and imprints a card from a graveyard")
    void acceptsGraveyardImprint() {
        Card imprinted = new CopperMyr();

        Permanent golem = castMirrorGolem(imprinted, true);

        harness.assertNotInGraveyard(player2, "Copper Myr");
        assertThat(gd.getImprintedCard(golem.getCard())).isSameAs(imprinted);
        assertThat(gd.getCardsExiledByPermanent(golem.getId())).containsExactly(imprinted);
    }

    @Test
    @DisplayName("Gains protection from the imprinted card's card types")
    void protectsFromImprintedCardTypes() {
        Permanent golem = castMirrorGolem(new CopperMyr(), true);
        Permanent shard = addCreatureReady(player2, new GraniteShard());
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, golem, shard)).isTrue();
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        int shardIndex = gd.playerBattlefields.get(player2.getId()).indexOf(shard);
        assertThatThrownBy(() -> harness.activateAbility(player2, shardIndex, 0, null, golem.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Gains protection from the creature type of an artifact creature imprint")
    void protectsFromCreatureTypeOfImprintedArtifactCreature() {
        Permanent golem = castMirrorGolem(new CopperMyr(), true);
        golem.setAttacking(true);
        addCreatureReady(player2, new FangrenHunter());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Declining the ETB ability grants no protection")
    void declinesGraveyardImprint() {
        Card declined = new CopperMyr();
        Permanent golem = castMirrorGolem(declined, false);
        assertThat(gd.getImprintedCard(golem.getCard())).isNull();
        assertThat(gd.getCardsExiledByPermanent(golem.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Copper Myr");

        Permanent shard = addCreatureReady(player2, new GraniteShard());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        int shardIndex = gd.playerBattlefields.get(player2.getId()).indexOf(shard);
        harness.activateAbility(player2, shardIndex, 0, null, golem.getId());
        harness.passBothPriorities();

        assertThat(golem.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent castMirrorGolem(Card graveyardCard, boolean accept) {
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MirrorGolem(), "{6}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);

        return findPermanent(player1, "Mirror Golem");
    }
}
