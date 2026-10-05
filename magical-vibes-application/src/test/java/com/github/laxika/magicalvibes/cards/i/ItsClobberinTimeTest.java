package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ItsClobberinTime.class, HillGiant.class, GrizzlyBears.class,
        MindStone.class, GloriousAnthem.class})
class ItsClobberinTimeTest extends BaseCardTest {

    @Test
    @DisplayName("The clobbering mode deals the source creature's power to an opponent's creature")
    void dealsPowerDamageToOpponentsCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ItsClobberinTime()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorcery(player1, 0, 0,
                List.of(source.getId(), harness.getPermanentId(player2, "Grizzly Bears")));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The destruction mode destroys an artifact or enchantment")
    void destroysArtifactOrEnchantment() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setHand(player1, List.of(new ItsClobberinTime()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorcery(player1, 0, 1,
                List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mind Stone");
    }

    @Test
    @DisplayName("The destruction mode also destroys an enchantment")
    void destroysEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new ItsClobberinTime()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorcery(player1, 0, 1,
                List.of(enchantment.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("The clobbering mode requires a creature controlled by an opponent as its second target")
    void rejectsOwnCreatureAsSecondTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ItsClobberinTime()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0,
                List.of(source.getId(), ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("A resolved spell with rebound is exiled for its next upkeep")
    void resolvedSpellIsExiledForRebound() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        ItsClobberinTime card = new ItsClobberinTime();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorcery(player1, 0, 1,
                List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void usesPowerAtResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareCast();
        harness.castModalSorcery(player1, 0, 0, List.of(source.getId(), victim.getId()));
        harness.addToBattlefield(player1, new GloriousAnthem());

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void rejectsOpponentsCreatureAsDamageSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0,
                List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDestroyOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        prepareCast();
        harness.castModalSorcery(player1, 0, 1, List.of(artifact.getId()));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mind Stone");
    }

    @Test
    void destructionModeRejectsAnOrdinaryCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void missingDamageSourcePreventsDamageButStillRebounds() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ItsClobberinTime card = prepareCast();
        harness.castModalSorcery(player1, 0, 0, List.of(source.getId(), victim.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.setGraveyard(player1, List.of(source.getCard()));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void missingOnlyTargetPreventsResolutionAndRebound() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        ItsClobberinTime card = prepareCast();
        harness.castModalSorcery(player1, 0, 1, List.of(artifact.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        harness.setGraveyard(player2, List.of(artifact.getCard()));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "It's Clobberin' Time!");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void damageSourceChangingControllersPreventsDamageButStillRebounds() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ItsClobberinTime card = prepareCast();
        harness.castModalSorcery(player1, 0, 0, List.of(source.getId(), victim.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void missingVictimPreventsDamageButStillRebounds() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ItsClobberinTime card = prepareCast();
        harness.castModalSorcery(player1, 0, 0, List.of(source.getId(), victim.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(victim);
        harness.setGraveyard(player2, List.of(victim.getCard()));

        harness.passBothPriorities();

        assertThat(source.getMarkedDamage()).isZero();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void reboundChoosesNewModeAndTargetsWhileCasting() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        ItsClobberinTime card = prepareCast();
        harness.castModalSorcery(player1, 0, 0, List.of(source.getId(), victim.getId()));
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Destroy target artifact or enchantment");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mind Stone");
        harness.assertInGraveyard(player1, "It's Clobberin' Time!");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void decliningReboundLeavesCardExiledWithoutAnotherOffer() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        ItsClobberinTime card = prepareCast();
        harness.castModalSorcery(player1, 0, 1, List.of(artifact.getId()));
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    private ItsClobberinTime prepareCast() {
        ItsClobberinTime card = new ItsClobberinTime();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        return card;
    }
}
