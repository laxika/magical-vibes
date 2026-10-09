package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrafnaFounderOfLatNam.class, CopperMyr.class, CounselOfTheSoratami.class, EnergyRefractor.class})
class DrafnaFounderOfLatNamTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target artifact you control to its owner's hand")
    void returnsTargetArtifactToHand() {
        addReadyDrafna();
        Permanent copperMyr = addCreatureReady(player1, new CopperMyr());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, copperMyr.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Copper Myr");
        harness.assertNotOnBattlefield(player1, "Copper Myr");
    }

    @Test
    @DisplayName("Copies an artifact spell as a token without granting haste")
    void copiesArtifactSpellAsToken() {
        addReadyDrafna();
        CopperMyr copperMyr = new CopperMyr();
        harness.setHand(player1, List.of(copperMyr));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.activateAbility(player1, 0, 1, null, copperMyr.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        assertThat(gd.pendingMayAbilities).isEmpty();

        resolveAllTriggers();

        List<Permanent> copperMyrs = findPermanents(player1, "Copper Myr");
        assertThat(copperMyrs).hasSize(2);
        Permanent copy = copperMyrs.stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(copy.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot return an artifact controlled by an opponent")
    void cannotReturnOpponentArtifact() {
        addReadyDrafna();
        Permanent opponentArtifact = addCreatureReady(player2, new CopperMyr());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot copy a nonartifact spell")
    void cannotCopyNonartifactSpell() {
        addReadyDrafna();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void artifactTokenRetainsItsActivatedAbilityAndEntersTrigger() {
        addReadyDrafna();
        EnergyRefractor refractor = new EnergyRefractor();
        harness.setHand(player1, List.of(refractor));
        harness.setLibrary(player1, List.of(new DrafnaFounderOfLatNam(), new DrafnaFounderOfLatNam()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.activateAbility(player1, 0, 1, null, refractor.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Energy Refractor")).hasSize(2);
        Permanent token = findPermanents(player1, "Energy Refractor").stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, tokenIndex, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(token.isTapped()).isFalse();
    }

    @Test
    void returnsBorrowedArtifactToItsOwnerWhileDrafnaIsTappedAndSummoningSick() {
        Permanent drafna = harness.addToBattlefieldAndReturn(player1, new DrafnaFounderOfLatNam());
        drafna.setSummoningSick(true);
        drafna.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new EnergyRefractor());
        harness.inMutationScope(() -> com.github.laxika.magicalvibes.testutil.GameTestEngineContext.get()
                .getBean(com.github.laxika.magicalvibes.service.battlefield.CreatureControlService.class)
                .applyControlEffect(gd, player1.getId(), artifact,
                        new com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect(
                                com.github.laxika.magicalvibes.model.effect.ControlDuration.PERMANENT),
                        com.github.laxika.magicalvibes.model.effect.EffectDuration.PERMANENT, null, "borrowed artifact"));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, artifact.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Energy Refractor");
        harness.assertNotInHand(player1, "Energy Refractor");
        harness.assertNotOnBattlefield(player1, "Energy Refractor");
    }

    @Test
    void cannotReturnNonartifactPermanent() {
        addReadyDrafna();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null,
                findPermanent(player1, "Drafna, Founder of Lat-Nam").getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCopyOpponentsArtifactSpell() {
        addReadyDrafna();
        EnergyRefractor artifact = new EnergyRefractor();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(artifact));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castArtifact(player2, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCopyWhileSummoningSick() {
        Permanent drafna = harness.addToBattlefieldAndReturn(player1, new DrafnaFounderOfLatNam());
        drafna.setSummoningSick(true);
        EnergyRefractor artifact = new EnergyRefractor();
        harness.setHand(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(drafna.isTapped()).isFalse();
    }

    @Test
    void returnAbilityDoesNotResolveIfArtifactChangesController() {
        addReadyDrafna();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, artifact.getId());

        harness.inMutationScope(() -> com.github.laxika.magicalvibes.testutil.GameTestEngineContext.get()
                .getBean(com.github.laxika.magicalvibes.service.battlefield.CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), artifact,
                        new com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect(
                                com.github.laxika.magicalvibes.model.effect.ControlDuration.PERMANENT),
                        com.github.laxika.magicalvibes.model.effect.EffectDuration.PERMANENT, null, "changed control"));
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Energy Refractor");
        harness.assertNotInHand(player1, "Energy Refractor");
        harness.assertNotInHand(player2, "Energy Refractor");
    }

    @Test
    void copyAbilityPaysThreeManaAndTapsDrafna() {
        addReadyDrafna();
        EnergyRefractor artifact = new EnergyRefractor();
        harness.setHand(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0);

        harness.activateAbility(player1, 0, 1, null, artifact.getId());

        assertThat(findPermanent(player1, "Drafna, Founder of Lat-Nam").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyDrafna() {
        addCreatureReady(player1, new DrafnaFounderOfLatNam());
    }
}
