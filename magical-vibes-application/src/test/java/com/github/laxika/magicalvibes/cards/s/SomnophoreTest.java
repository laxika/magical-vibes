package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Somnophore.class, CoralMerfolk.class, HermeticStudy.class})
class SomnophoreTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage chooses a creature as the trigger is put on the stack")
    void choosesDamagedPlayersCreatureWhenTriggerIsPutOnStack() {
        Permanent somnophore = addCreatureReady(player1, new Somnophore());
        somnophore.setAttacking(true);
        Permanent damagedPlayersCreature = addCreatureReady(player2, new CoralMerfolk());
        Permanent controllersCreature = addCreatureReady(player1, new CoralMerfolk());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(damagedPlayersCreature.getId())
                .doesNotContain(controllersCreature.getId());
    }

    @Test
    @DisplayName("The chosen creature stays tapped while Somnophore remains on the battlefield")
    void chosenCreatureStaysTappedUntilSourceLeaves() {
        Permanent somnophore = addCreatureReady(player1, new Somnophore());
        somnophore.setAttacking(true);
        Permanent damagedPlayersCreature = addCreatureReady(player2, new CoralMerfolk());

        resolveCombat();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(damagedPlayersCreature.getId());
        harness.handlePermanentChosen(player1, damagedPlayersCreature.getId());
        harness.passBothPriorities();

        assertThat(damagedPlayersCreature.isTapped()).isTrue();
        assertThat(damagedPlayersCreature.getUntapPreventedWhileSourceOnBattlefieldIds())
                .contains(somnophore.getId());

        advanceToUpkeep(player2);
        assertThat(damagedPlayersCreature.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(somnophore);
        advanceToUpkeep(player1);
        advanceToUpkeep(player2);

        assertThat(damagedPlayersCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Noncombat damage also chooses a creature as the trigger is put on the stack")
    void noncombatDamageChoosesTargetWhenTriggerIsPutOnStack() {
        Permanent somnophore = addCreatureReady(player1, new Somnophore());
        Permanent study = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        study.setAttachedTo(somnophore.getId());
        Permanent damagedPlayersCreature = addCreatureReady(player2, new CoralMerfolk());

        int somnophoreIndex = gd.playerBattlefields.get(player1.getId()).indexOf(somnophore);
        harness.activateAbility(player1, somnophoreIndex, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(damagedPlayersCreature.getId());
    }
}
