package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.e.ElectrostaticBolt;
import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.cards.g.GraniteShard;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.cards.s.Shatter;
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

@CardUsed({MirrorGolem.class, CopperMyr.class, GraniteShard.class, FangrenHunter.class,
        ElectrostaticBolt.class, Shatter.class, Panharmonicon.class})
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

    @Test
    @DisplayName("Can imprint a card from its controller's graveyard")
    void imprintsFromOwnGraveyard() {
        Card imprinted = new Shatter();
        harness.setGraveyard(player1, List.of(imprinted));
        harness.castFromHand(player1, new MirrorGolem(), "{6}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(imprinted.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent golem = findPermanent(player1, "Mirror Golem");
        harness.assertNotInGraveyard(player1, "Shatter");
        assertThat(gd.getCardsExiledByPermanent(golem.getId())).containsExactly(imprinted);
    }

    @Test
    @DisplayName("An instant imprint prevents instant spells from targeting the Golem")
    void protectsFromInstantSpells() {
        Permanent golem = castMirrorGolem(new Shatter(), true);
        harness.setHand(player2, List.of(new ElectrostaticBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, golem.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts does not prevent damage from an instant")
    void artifactProtectionDoesNotProtectFromInstants() {
        castMirrorGolem(new GraniteShard(), true);
        harness.setHand(player2, List.of(new ElectrostaticBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Mirror Golem"));

        harness.assertInGraveyard(player1, "Mirror Golem");
    }

    @Test
    @DisplayName("Protection from creatures prevents combat damage while blocking")
    void preventsCreatureCombatDamage() {
        Permanent golem = castMirrorGolem(new CopperMyr(), true);
        addCreatureReady(player2, new FangrenHunter());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Mirror Golem");
        assertThat(golem.getMarkedDamage()).isZero();
        assertThat(findPermanent(player2, "Fangren Hunter").getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with empty graveyards does not ask for an imprint")
    void emptyGraveyardsGrantNoProtection() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.castFromHand(player1, new MirrorGolem(), "{6}");
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent golem = findPermanent(player1, "Mirror Golem");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getCardsExiledByPermanent(golem.getId())).isEmpty();
        Permanent shard = addCreatureReady(player2, new GraniteShard());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(shard),
                0, null, golem.getId());
        harness.passBothPriorities();

        assertThat(golem.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses protection when the imprinted card leaves exile")
    void losesProtectionWhenImprintLeavesExile() {
        Card imprinted = new CopperMyr();
        Permanent golem = castMirrorGolem(imprinted, true);
        gd.removeFromExile(imprinted.getId());
        harness.setGraveyard(player2, List.of(imprinted));
        Permanent shard = addCreatureReady(player2, new GraniteShard());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(shard),
                0, null, golem.getId());
        harness.passBothPriorities();

        assertThat(golem.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple imprint triggers grant protection from every exiled card's types")
    void protectsFromAllCardsExiledByMultipleImprintTriggers() {
        Card artifact = new GraniteShard();
        Card instant = new Shatter();
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.setGraveyard(player2, List.of(artifact, instant));
        harness.castFromHand(player1, new MirrorGolem(), "{6}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent golem = findPermanent(player1, "Mirror Golem");
        assertThat(gd.getCardsExiledByPermanent(golem.getId())).containsExactlyInAnyOrder(artifact, instant);
        harness.setHand(player2, List.of(new ElectrostaticBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, golem.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        Permanent shard = addCreatureReady(player2, new GraniteShard());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(shard), 0, null, golem.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
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
