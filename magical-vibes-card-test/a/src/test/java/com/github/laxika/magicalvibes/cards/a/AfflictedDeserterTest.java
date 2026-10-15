package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.h.Helvault;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AfflictedDeserter.class, Ornithopter.class, DarksteelRelic.class,
        Helvault.class, LeylineOfTheVoid.class})
class AfflictedDeserterTest extends BaseCardTest {

    @Test
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new AfflictedDeserter());
        Permanent deserter = findPermanent(player1, "Afflicted Deserter");
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(deserter.isTransformed()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new AfflictedDeserter());
        Permanent deserter = findPermanent(player1, "Afflicted Deserter");
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(deserter.isTransformed()).isFalse();
    }

    @Test
    void transformTriggerDestroysArtifactAndDealsDamage() {
        harness.addToBattlefield(player2, new Ornithopter());
        Permanent artifact = findPermanent(player2, "Ornithopter");
        int lifeBefore = gd.getLife(player2.getId());

        transformAndChooseArtifact(artifact);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertLife(player2, lifeBefore - 3);
        assertThat(findPermanent(player1, "Werewolf Ransacker").isTransformed()).isTrue();
    }

    @Test
    void transformTriggerCanBeDeclined() {
        harness.addToBattlefield(player2, new Ornithopter());
        Permanent artifact = findPermanent(player2, "Ornithopter");
        int lifeBefore = gd.getLife(player2.getId());

        transformAndChooseArtifact(artifact);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertLife(player2, lifeBefore);
        assertThat(findPermanent(player1, "Werewolf Ransacker").isTransformed()).isTrue();
    }

    @Test
    void noDamageWhenIndestructibleArtifact() {
        harness.addToBattlefield(player2, new DarksteelRelic());
        Permanent relic = findPermanent(player2, "Darksteel Relic");
        int lifeBefore = gd.getLife(player2.getId());

        transformAndChooseArtifact(relic);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Darksteel Relic");
        harness.assertLife(player2, lifeBefore);
    }

    @Test
    void werewolfTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new AfflictedDeserter());
        Permanent deserter = findPermanent(player1, "Afflicted Deserter");
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(deserter.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(deserter.isTransformed()).isFalse();
    }

    @Test
    void werewolfDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new AfflictedDeserter());
        Permanent deserter = findPermanent(player1, "Afflicted Deserter");
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(deserter.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(deserter.isTransformed()).isTrue();
    }

    @Test
    void transformTriggerNoArtifacts() {
        harness.addToBattlefield(player1, new AfflictedDeserter());
        Permanent deserter = findPermanent(player1, "Afflicted Deserter");
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(deserter.isTransformed()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDestroyOwnArtifactAndDamageItsController() {
        harness.addToBattlefield(player1, new Helvault());
        Permanent artifact = findPermanent(player1, "Helvault");
        int lifeBefore = gd.getLife(player1.getId());

        transformAndChooseArtifact(artifact);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Helvault");
        harness.assertNotOnBattlefield(player1, "Helvault");
        harness.assertLife(player1, lifeBefore - 3);
    }

    @Test
    void noDamageWhenArtifactIsExiledInsteadOfEnteringGraveyard() {
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        harness.addToBattlefield(player2, new Ornithopter());
        Permanent artifact = findPermanent(player2, "Ornithopter");
        int lifeBefore = gd.getLife(player2.getId());

        transformAndChooseArtifact(artifact);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertNotInGraveyard(player2, "Ornithopter");
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card()).isSameAs(artifact.getCard());
            assertThat(entry.ownerId()).isEqualTo(player2.getId());
        });
        harness.assertLife(player2, lifeBefore);
    }

    private void transformAndChooseArtifact(Permanent artifact) {
        harness.addToBattlefield(player1, new AfflictedDeserter());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // The target is chosen when the trigger goes on the stack, before its optional effect resolves.
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.assertOnBattlefield(player1, "Werewolf Ransacker");
        resolveAllTriggers();
    }
}
