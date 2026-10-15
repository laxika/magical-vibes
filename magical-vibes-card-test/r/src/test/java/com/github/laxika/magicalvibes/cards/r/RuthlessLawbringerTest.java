package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BanditsHaul;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VoraciousVarmint;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuthlessLawbringer.class, VoraciousVarmint.class, Forest.class, BanditsHaul.class})
class RuthlessLawbringerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may sacrifice another creature to destroy a target nonland permanent")
    void etbSacrificeDestroysTargetNonlandPermanent() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new VoraciousVarmint());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoraciousVarmint());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        RuthlessLawbringer lawbringer = new RuthlessLawbringer();
        harness.castFromHand(player1, lawbringer, "{1}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice sacrificeChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(sacrificeChoice.validIds()).containsExactly(sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());

        PendingInteraction.PermanentChoice targetChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(targetChoice.validIds()).contains(target.getId()).doesNotContain(land.getId());
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(lawbringer.getId()));
    }

    @Test
    @DisplayName("Declining the ETB sacrifice leaves permanents unchanged")
    void decliningEtbSacrificeDoesNothing() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new VoraciousVarmint());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoraciousVarmint());
        harness.castFromHand(player1, new RuthlessLawbringer(), "{1}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Without another creature, neither the source nor an opponent's creature can be sacrificed")
    void noOtherControlledCreatureDoesNotDestroyAnything() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoraciousVarmint());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BanditsHaul());
        harness.castFromHand(player1, new RuthlessLawbringer(), "{1}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ruthless Lawbringer");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The reflexive trigger can destroy a noncreature artifact")
    void sacrificeDestroysNoncreatureArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new VoraciousVarmint());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BanditsHaul());
        harness.castFromHand(player1, new RuthlessLawbringer(), "{1}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("The reflexive trigger can destroy Ruthless Lawbringer itself")
    void canTargetSourceAfterSacrificingAnotherCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new VoraciousVarmint());
        harness.castFromHand(player1, new RuthlessLawbringer(), "{1}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Ruthless Lawbringer"));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        harness.assertInGraveyard(player1, "Ruthless Lawbringer");
        harness.assertNotOnBattlefield(player1, "Ruthless Lawbringer");
    }

    @Test
    @DisplayName("An opponent can respond to the reflexive trigger without undoing the sacrifice")
    void targetCanSacrificeItselfInResponse() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new VoraciousVarmint());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BanditsHaul());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoraciousVarmint());
        harness.castFromHand(player1, new RuthlessLawbringer(), "{1}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, target.getId());

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, artifact.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard(), artifact.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertOnBattlefield(player1, "Ruthless Lawbringer");
        assertThat(gd.stack).isEmpty();
    }
}
