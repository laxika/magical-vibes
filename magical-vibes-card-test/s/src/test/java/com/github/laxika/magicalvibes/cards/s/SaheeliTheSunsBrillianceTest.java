package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HuntersBlowgun;
import com.github.laxika.magicalvibes.cards.m.MalametBrawler;
import com.github.laxika.magicalvibes.model.CardType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaheeliTheSunsBrilliance.class, MalametBrawler.class, HuntersBlowgun.class, SelfReflection.class})
class SaheeliTheSunsBrillianceTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an artifact token copy with haste and a next-end-step sacrifice")
    void createsArtifactTokenCopyWithHasteAndSacrifice() {
        Permanent saheeli = addReadySaheeli(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(saheeli.isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Malamet Brawler").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Malamet Brawler").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("Can target an artifact you control")
    void canTargetArtifactYouControl() {
        addReadySaheeli(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HuntersBlowgun());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Hunter's Blowgun").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an opponent's permanent or Saheeli herself")
    void rejectsOpponentAndSelfTargets() {
        Permanent saheeli = addReadySaheeli(player1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MalametBrawler());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature or artifact you control");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, saheeli.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature or artifact you control");
    }

    @Test
    @DisplayName("Copying Saheeli's token copies the artifact exception but not granted haste or sacrifice")
    void furtherCopyDoesNotInheritGrantedHasteOrSacrifice() {
        addReadySaheeli(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        Permanent firstToken = findPermanents(player1, "Malamet Brawler").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.setHand(player1, List.of(new SelfReflection()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castAndResolveSorcery(player1, 0, firstToken.getId());

        Permanent secondToken = findPermanents(player1, "Malamet Brawler").stream()
                .filter(permanent -> permanent.getCard().isToken() && !permanent.getId().equals(firstToken.getId()))
                .findFirst().orElseThrow();
        assertThat(secondToken.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(secondToken.getCard().getKeywords()).doesNotContain(Keyword.HASTE);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondToken).doesNotContain(firstToken);
    }

    @Test
    @DisplayName("The original controller cannot sacrifice a token now controlled by an opponent")
    void opponentControlledTokenSurvivesDelayedSacrifice() {
        addReadySaheeli(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Malamet Brawler").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        gd.playerBattlefields.get(player1.getId()).remove(token);
        gd.playerBattlefields.get(player2.getId()).add(token);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
    }

    @Test
    @DisplayName("The ability resolves after Saheeli leaves the battlefield")
    void resolvesWithoutSaheeli() {
        Permanent saheeli = addReadySaheeli(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(saheeli);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Malamet Brawler"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("The ability creates no token if its target changes controller before resolution")
    void targetMustStillBeControlledOnResolution() {
        addReadySaheeli(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability creates no token if its target leaves before resolution")
    void missingTargetCreatesNoToken() {
        addReadySaheeli(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Saheeli cannot activate her tap ability while summoning sick")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new SaheeliTheSunsBrilliance());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A token created during an end step waits for the following end step")
    void endStepCreationWaitsUntilFollowingEndStep() {
        addReadySaheeli(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Malamet Brawler").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.passUntilWithNoAttackers(player2, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    private Permanent addReadySaheeli(Player player) {
        return addCreatureReady(player, new SaheeliTheSunsBrilliance());
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
