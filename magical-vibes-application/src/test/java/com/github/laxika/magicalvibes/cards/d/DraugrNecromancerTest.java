package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DraugrNecromancer.class, Forest.class, GrizzlyBears.class, Shock.class})
class DraugrNecromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's nontoken creature with an ice counter instead of letting it die")
    void exilesOpponentNontokenCreatureWithIceCounter() {
        UUID bearId = destroyOpponentCreature(new GrizzlyBears());

        assertThat(gd.findExiledCard(bearId)).isNotNull();
        assertThat(gd.exiledCardsWithIceCounters).contains(bearId);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(
                gd.findExiledCard(bearId).card());
    }

    @Test
    @DisplayName("Does not exile a token with an ice counter")
    void doesNotExileTokenWithIceCounter() {
        Card token = createTokenCreature();
        UUID tokenId = token.getId();
        Permanent tokenPermanent = addCreatureReady(player2, token);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, tokenPermanent.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(tokenId)).isNull();
        assertThat(gd.exiledCardsWithIceCounters).doesNotContain(tokenId);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("Casts an opponent-owned ice-counter card with snow mana as any color")
    void castsIceCounterCardWithSnowManaAsAnyColor() {
        UUID bearId = destroyOpponentCreature(new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, bearId))
                .isInstanceOf(IllegalStateException.class);

        gd.playerManaPools.get(player1.getId()).clear();
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 2);

        harness.castFromExile(player1, bearId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not grant permission to play an ice-counter land")
    void doesNotGrantPermissionForIceCounterLand() {
        Forest forest = new Forest();
        gd.addToExileWithIceCounter(player2.getId(), forest);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private UUID destroyOpponentCreature(Card creature) {
        harness.addToBattlefield(player1, new DraugrNecromancer());
        Permanent permanent = addCreatureReady(player2, creature);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, permanent.getId());
        harness.passBothPriorities();
        return creature.getId();
    }

    private Card createTokenCreature() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
