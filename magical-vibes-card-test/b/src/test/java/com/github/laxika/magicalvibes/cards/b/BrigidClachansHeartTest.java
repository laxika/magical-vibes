package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.BrigidDounsMind;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PummelerForHire;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrigidClachansHeart.class, BrigidDounsMind.class, PummelerForHire.class, Island.class})
class BrigidClachansHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Kithkin token when it enters the battlefield")
    void createsKithkinOnEnter() {
        harness.setHand(player1, List.of(new BrigidClachansHeart()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countKithkinTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Transforms into Brigid, Doun's Mind after paying green in the first main phase")
    void transformsToBackFaceAfterPayingGreen() {
        Permanent brigid = addFrontFace(player1);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(brigid.isTransformed()).isTrue();
        assertThat(brigid.getCard().getName()).isEqualTo("Brigid, Doun's Mind");
    }

    @Test
    @DisplayName("Transforms back and creates a Kithkin token after paying white in the first main phase")
    void transformsToFrontFaceAfterPayingWhite() {
        Permanent brigid = addBackFace(player1);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(brigid.isTransformed()).isFalse();
        assertThat(brigid.getCard().getName()).isEqualTo("Brigid, Clachan's Heart");
        assertThat(countKithkinTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Back face adds green mana for each other creature")
    void backFaceAddsGreenForOtherCreatures() {
        Permanent brigid = addBackFace(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        harness.activateAbility(player1, indexOf(player1, brigid), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Back face adds white mana for each other creature")
    void backFaceAddsWhiteForOtherCreatures() {
        Permanent brigid = addBackFace(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        harness.activateAbility(player1, indexOf(player1, brigid), 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(3);
    }

    @Test
    void entryCreatesGreenAndWhiteOneOneCreatureToken() {
        harness.enterBattlefieldAndReturn(player1, new BrigidClachansHeart());
        resolveAllTriggers();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(perm -> perm.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.KITHKIN);
        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(countKithkinTokens(player2)).isZero();
    }

    @Test
    void entryTriggerStillCreatesTokenAfterBrigidLeaves() {
        Permanent brigid = harness.enterBattlefieldAndReturn(player1, new BrigidClachansHeart());
        gd.playerBattlefields.get(player1.getId()).remove(brigid);
        resolveAllTriggers();

        assertThat(countKithkinTokens(player1)).isEqualTo(1);
    }

    @Test
    void canDeclineGreenPaymentWithoutTransformingOrSpendingMana() {
        Permanent brigid = addFrontFace(player1);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(brigid.isTransformed()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(countKithkinTokens(player1)).isZero();
    }

    @Test
    void canDeclineWhitePaymentWithoutTransformingOrCreatingToken() {
        Permanent brigid = addBackFace(player1);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(brigid.isTransformed()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(countKithkinTokens(player1)).isZero();
    }

    @Test
    void payingGreenSpendsManaAndDoesNotCreateAnotherToken() {
        Permanent brigid = addFrontFace(player1);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(brigid.isTransformed()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(countKithkinTokens(player1)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingWhiteSpendsManaAndQueuesTokenCreationSeparately() {
        Permanent brigid = addBackFace(player1);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(brigid.isTransformed()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(countKithkinTokens(player1)).isZero();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(countKithkinTokens(player1)).isEqualTo(1);
    }

    @Test
    void onlyActivePlayersBrigidTriggersInFirstMainPhase() {
        Permanent front = addFrontFace(player1);
        Permanent back = addBackFace(player2);
        advanceToPrecombatMain(player2);

        assertThat(front.isTransformed()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(back.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(countKithkinTokens(player1)).isZero();
    }

    @Test
    void doesNotTriggerInPostcombatMainPhase() {
        addFrontFace(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countKithkinTokens(player1)).isZero();
    }

    @Test
    void backFaceProducesNoManaWithoutOtherCreaturesButStillTaps() {
        Permanent brigid = addBackFace(player1);
        harness.addToBattlefield(player1, new Island());
        addCreatureReady(player2);

        harness.activateAbility(player1, indexOf(player1, brigid), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(brigid.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaAbilityCountsOnlyControlledCreaturesAndProducesOneColorImmediately() {
        Permanent brigid = addBackFace(player1);
        addCreatureReady(player1);
        harness.addToBattlefield(player1, new Island());
        addCreatureReady(player2);
        addCreatureReady(player2);

        harness.activateAbility(player1, indexOf(player1, brigid), 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(brigid.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, brigid), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transformingDoesNotRemoveSummoningSickness() {
        Permanent brigid = addFrontFace(player1);
        brigid.setSummoningSick(true);
        addCreatureReady(player1);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(brigid.isTransformed()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, brigid), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private Permanent addFrontFace(Player player) {
        return addCreatureReady(player, new BrigidClachansHeart());
    }

    private Permanent addBackFace(Player player) {
        Permanent perm = addFrontFace(player);
        perm.setCard(perm.getOriginalCard().getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }

    private Permanent addCreatureReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new PummelerForHire());
    }

    private long countKithkinTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(perm -> perm.getCard().isToken())
                .filter(perm -> perm.getCard().getSubtypes().contains(CardSubtype.KITHKIN))
                .count();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
