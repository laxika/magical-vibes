package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;




@CardUsed({GrenzoHavocRaiser.class, Divination.class, GrizzlyBears.class, Forest.class})
class GrenzoHavocRaiserTest extends BaseCardTest {

    private static final String GOAD_MODE = "Goad target creature";
    private static final String EXILE_MODE = "Exile the top card";

    @Test
    @DisplayName("Goad mode lets the controller choose a creature controlled by the damaged player")
    void goadsCreatureControlledByDamagedPlayer() {
        Permanent grenzo = addCreatureReady(player1, new GrenzoHavocRaiser());
        grenzo.setAttacking(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, GOAD_MODE);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exile mode grants an any-color cast permission until end of turn")
    void exilesTopCardAndAllowsAnyColorCast() {
        Permanent grenzo = addCreatureReady(player1, new GrenzoHavocRaiser());
        grenzo.setAttacking(true);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBeforeCast = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, EXILE_MODE);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).contains(topCard.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeCast + 2);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }
    @Test
    @DisplayName("The exile mode grants an end-of-turn any-color cast permission")
    void exileModeGrantsCastPermission() {
        addGrenzoAttacking();
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);

        resolveGrenzoTrigger("Exile the top card");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).contains(topCard.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("The exile mode does not grant land-play permission")
    void exileModeDoesNotGrantLandPermission() {
        addGrenzoAttacking();
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        resolveGrenzoTrigger("Exile the top card");

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The goad mode targets a creature controlled by the damaged player")
    void goadModeTargetsDamagedPlayerCreature() {
        addGrenzoAttacking();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveGrenzoTrigger("Goad target creature");
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    private void addGrenzoAttacking() {
        addCreatureReady(player1, new GrenzoHavocRaiser());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
    }

    private void resolveGrenzoTrigger(String mode) {
        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, mode);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Mode and goad target are chosen before opponents can respond to the trigger")
    void choosesModeAndTargetBeforeResolution() {
        addGrenzoAttacking();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, GOAD_MODE);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(als.getMustAttackRequirementCount(gd, target)).isZero();

        resolveAllTriggers();

        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Goad cannot be chosen when the damaged player controls no legal creature target")
    void cannotChooseGoadWithoutLegalTarget() {
        addGrenzoAttacking();
        harness.setLibrary(player2, List.of(new Forest()));

        resolveCombat();
        resolveAllTriggers();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options().stream()
                .filter(option -> !choice.disabledOptions().contains(option)).toList())
                .contains(EXILE_MODE)
                .doesNotContain(GOAD_MODE);
    }

    @Test
    @DisplayName("Exile mode does nothing when the damaged player's library is empty")
    void emptyLibraryDoesNotGrantPermission() {
        addGrenzoAttacking();
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of());

        resolveGrenzoTrigger(EXILE_MODE);

        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).isEmpty();
        assertThat(gd.exilePlayAnyManaType).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An uncast exiled card stays in exile after its permission expires")
    void castPermissionExpiresAtEndOfTurn() {
        addGrenzoAttacking();
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard, new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveGrenzoTrigger(EXILE_MODE);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(topCard.getId());
    }

}
