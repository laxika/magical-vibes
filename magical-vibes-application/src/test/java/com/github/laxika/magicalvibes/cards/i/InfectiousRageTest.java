package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AnuridBarkripper;
import com.github.laxika.magicalvibes.cards.a.AvenWarcraft;
import com.github.laxika.magicalvibes.cards.b.BattlewiseAven;
import com.github.laxika.magicalvibes.cards.c.CephalidInkshrouder;
import com.github.laxika.magicalvibes.cards.g.Glory;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SylvanSafekeeper;
import com.github.laxika.magicalvibes.cards.t.ToxicStench;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnuridBarkripper.class, AvenWarcraft.class, BattlewiseAven.class, CephalidInkshrouder.class, Glory.class, InfectiousRage.class, KrosanVerge.class, SylvanSafekeeper.class, ToxicStench.class})
class InfectiousRageTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/-1")
    void boostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new AnuridBarkripper());
        attachRageTo(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("When enchanted creature dies, Infectious Rage attaches to the only legal creature, including a shrouded creature")
    void returnsToOnlyLegalCreatureWithoutTargeting() {
        Permanent dyingCreature = addCreatureReady(player1, new AnuridBarkripper());
        Permanent shroudedCreature = addShroudedCreature();
        attachRageTo(player1, dyingCreature);

        destroyCreature(dyingCreature);

        Permanent aura = findPermanent(player1, "Infectious Rage");
        assertThat(aura.getAttachedTo()).isEqualTo(shroudedCreature.getId());
    }

    @Test
    @DisplayName("When enchanted creature dies, Infectious Rage chooses randomly among legal creatures")
    void choosesAmongLegalCreatures() {
        Permanent dyingCreature = addCreatureReady(player1, new AnuridBarkripper());
        Permanent firstCandidate = addCreatureReady(player1, new AnuridBarkripper());
        Permanent secondCandidate = addCreatureReady(player2, new AnuridBarkripper());
        attachRageTo(player1, dyingCreature);

        destroyCreature(dyingCreature);

        Permanent aura = findPermanent(player1, "Infectious Rage");
        assertThat(List.of(firstCandidate.getId(), secondCandidate.getId())).contains(aura.getAttachedTo());
    }

    @Test
    @DisplayName("When enchanted creature dies with no legal creature, Infectious Rage stays in the graveyard")
    void staysInGraveyardWithoutLegalCreature() {
        Permanent dyingCreature = addCreatureReady(player1, new AnuridBarkripper());
        attachRageTo(player1, dyingCreature);

        destroyCreature(dyingCreature);

        harness.assertInGraveyard(player1, "Infectious Rage");
        harness.assertNotOnBattlefield(player1, "Infectious Rage");
    }

    private Permanent attachRageTo(Player controller, Permanent creature) {
        Permanent auraPermanent = harness.addToBattlefieldAndReturn(controller, new InfectiousRage());
        auraPermanent.setAttachedTo(creature.getId());
        return auraPermanent;
    }

    private Permanent addShroudedCreature() {
        Permanent creature = addCreatureReady(player2, new CephalidInkshrouder());

        harness.setHand(player2, List.of(new AvenWarcraft()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0);

        harness.setHand(player2, List.of(new ToxicStench()));
        int creatureIndex = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        harness.activateAbility(player2, creatureIndex, null, null);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
        return creature;
    }

    private void destroyCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ToxicStench()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Can be cast attached to a creature")
    void castsAttachedToCreature() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new InfectiousRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Infectious Rage");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("When enchanted creature dies, Infectious Rage attaches to the only legal creature, including a shrouded creature")
    void returnsToOnlyLegalCreatureWithoutTargetingJudReview() {
        Permanent dyingCreature = addCreatureReady(player1, new SylvanSafekeeper());
        Permanent shroudedCreature = addCreatureReady(player1, new BattlewiseAven());
        harness.addToBattlefieldAndReturn(player1, new KrosanVerge());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, shroudedCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, shroudedCreature, Keyword.SHROUD)).isTrue();

        attachRageTo(player1, dyingCreature);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Infectious Rage");
        assertThat(aura.getAttachedTo()).isEqualTo(shroudedCreature.getId());
        assertThat(gqs.getEffectivePower(gd, shroudedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shroudedCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("When the only remaining creature is protected from red, Infectious Rage stays in the graveyard")
    void doesNotAttachToCreatureProtectedFromRed() {
        Permanent dyingCreature = addCreatureReady(player2, new BattlewiseAven());
        Permanent protectedCreature = addCreatureReady(player1, new BattlewiseAven());
        harness.setGraveyard(player1, List.of(new Glory()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.hasProtectionFrom(gd, protectedCreature, CardColor.RED))
                .isTrue();

        attachRageTo(player1, dyingCreature);
        destroyCreature(dyingCreature);

        harness.assertInGraveyard(player1, "Infectious Rage");
        harness.assertNotOnBattlefield(player1, "Infectious Rage");
    }

    @Test
    @DisplayName("Returns under its owner's control when an opponent's enchanted creature dies")
    void returnsAfterOpponentsCreatureDies() {
        Permanent dyingCreature = addCreatureReady(player2, new SylvanSafekeeper());
        Permanent survivor = addCreatureReady(player2, new BattlewiseAven());
        attachRageTo(player1, dyingCreature);

        harness.runStateBasedActions();
        resolveAllTriggers();

        Permanent aura = findPermanent(player1, "Infectious Rage");
        assertThat(aura.getAttachedTo()).isEqualTo(survivor.getId());
        harness.assertNotOnBattlefield(player2, "Infectious Rage");
        harness.assertNotInGraveyard(player1, "Infectious Rage");
    }

    @Test
    @DisplayName("Does not return if the Aura leaves the graveyard before its trigger resolves")
    void doesNotReturnFromExile() {
        Permanent dyingCreature = addCreatureReady(player1, new SylvanSafekeeper());
        addCreatureReady(player2, new BattlewiseAven());
        Card auraCard = attachRageTo(player1, dyingCreature).getCard();

        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Infectious Rage");
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(dyingCreature.getCard()));
        harness.setExile(player1, List.of(auraCard));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Infectious Rage");
        assertThat(gd.findExiledCard(auraCard.getId())).isNotNull();
    }
}
