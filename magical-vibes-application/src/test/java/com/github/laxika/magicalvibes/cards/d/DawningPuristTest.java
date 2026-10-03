package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AstralSlide;
import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawningPurist.class, AstralSlide.class, BarkhideMauler.class})
class DawningPuristTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the combat damage trigger destroys an enchantment controlled by the damaged player")
    void destroysDamagedPlayersEnchantment() {
        attackWithPurist();
        Permanent slide = harness.addToBattlefieldAndReturn(player2, new AstralSlide());

        resolveCombat();

        harness.handlePermanentChosen(player1, slide.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Astral Slide");
        harness.assertInGraveyard(player2, "Astral Slide");
    }

    @Test
    @DisplayName("Declining the combat damage trigger leaves the enchantment on the battlefield")
    void declineLeavesEnchantment() {
        attackWithPurist();
        harness.addToBattlefield(player2, new AstralSlide());

        resolveCombat();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Astral Slide"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Astral Slide");
    }

    @Test
    @DisplayName("Only enchantments controlled by the damaged player are legal choices")
    void onlyDamagedPlayersEnchantmentsAreChoices() {
        attackWithPurist();
        Permanent ownSlide = harness.addToBattlefieldAndReturn(player1, new AstralSlide());
        Permanent enemySlide = harness.addToBattlefieldAndReturn(player2, new AstralSlide());
        Permanent enemyCreature = harness.addToBattlefieldAndReturn(player2, new BarkhideMauler());

        resolveCombat();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(enemySlide.getId())
                .doesNotContain(ownSlide.getId(), enemyCreature.getId());
    }

    @Test
    @DisplayName("The combat damage trigger is skipped when the damaged player controls no enchantments")
    void noTriggerWithoutValidEnchantmentTarget() {
        attackWithPurist();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dawning Purist can be cast face down and turned face up for its morph cost")
    void canBeMorphed() {
        Permanent purist = castFaceDownPurist();
        assertThat(purist.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int puristIndex = gd.playerBattlefields.get(player1.getId()).indexOf(purist);
        harness.turnFaceUp(player1, puristIndex);
        harness.passBothPriorities();

        assertThat(purist.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("A face-down Dawning Purist deals combat damage without its enchantment destruction trigger")
    void faceDownCombatDamageDoesNotTrigger() {
        Permanent purist = castFaceDownPurist();
        purist.setSummoningSick(false);
        purist.setAttacking(true);
        harness.addToBattlefield(player2, new AstralSlide());

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Astral Slide");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Turning Dawning Purist face up before combat damage enables its enchantment destruction trigger")
    void turningFaceUpBeforeCombatDamageEnablesTrigger() {
        Permanent purist = castFaceDownPurist();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(purist));
        purist.setSummoningSick(false);
        purist.setAttacking(true);
        Permanent slide = harness.addToBattlefieldAndReturn(player2, new AstralSlide());

        resolveCombat();
        harness.handlePermanentChosen(player1, slide.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player2, "Astral Slide");
        harness.assertInGraveyard(player2, "Astral Slide");
    }

    @Test
    @DisplayName("The target becomes illegal if the damaged player no longer controls it at resolution")
    void targetChangingControllerIsNotDestroyed() {
        attackWithPurist();
        Permanent slide = harness.addToBattlefieldAndReturn(player2, new AstralSlide());

        resolveCombat();
        harness.handlePermanentChosen(player1, slide.getId());
        gd.playerBattlefields.get(player2.getId()).remove(slide);
        gd.playerBattlefields.get(player1.getId()).add(slide);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Astral Slide");
        harness.assertNotInGraveyard(player2, "Astral Slide");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castFaceDownPurist() {
        harness.setHand(player1, List.of(new DawningPurist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Dawning Purist");
    }

    private void attackWithPurist() {
        Permanent purist = addCreatureReady(player1, new DawningPurist());
        purist.setAttacking(true);
    }
}
