package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GreensideWatcher;
import com.github.laxika.magicalvibes.cards.r.RazortipWhip;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StolenIdentity.class, RazortipWhip.class, GreensideWatcher.class, SimicGuildgate.class, TurnToFrog.class})
class StolenIdentityTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of target artifact")
    void createsTokenCopyOfArtifact() {
        harness.addToBattlefield(player2, new RazortipWhip());
        harness.setHand(player1, List.of(new StolenIdentity()));
        addStolenIdentityMana();

        UUID targetId = harness.getPermanentId(player2, "Razortip Whip");
        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Razortip Whip"))
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(1);
    }

    @Test
    @DisplayName("Rejects a non-artifact non-creature target")
    void rejectsInvalidTarget() {
        harness.addToBattlefield(player1, new SimicGuildgate());
        harness.setHand(player1, List.of(new StolenIdentity()));
        addStolenIdentityMana();

        UUID targetId = harness.getPermanentId(player1, "Simic Guildgate");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can encode the resolving spell on the token it creates")
    void canEncodeOnCreatedToken() {
        harness.addToBattlefield(player2, new GreensideWatcher());
        harness.setHand(player1, List.of(new StolenIdentity()));
        addStolenIdentityMana();

        UUID targetId = harness.getPermanentId(player2, "Greenside Watcher");
        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = findPermanent(player1, "Greenside Watcher");
        assertThat(token.getCard().isToken()).isTrue();
        harness.handlePermanentChosen(player1, token.getId());

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Stolen Identity"));
        harness.assertNotInGraveyard(player1, "Stolen Identity");
    }

    @Test
    void decliningCipherKeepsTheCreatedCreatureAndPutsSpellInGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreensideWatcher());
        harness.setHand(player1, List.of(new StolenIdentity()));
        addStolenIdentityMana();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Greenside Watcher")).hasSize(1);
        assertThat(findPermanent(player1, "Greenside Watcher").getCard().isToken()).isTrue();
        harness.assertInGraveyard(player1, "Stolen Identity");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void illegalTargetPreventsTokenCreationAndEncoding() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreensideWatcher());
        addCreatureReady(player1, new GreensideWatcher());
        harness.setHand(player1, List.of(new StolenIdentity()));
        addStolenIdentityMana();

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Greenside Watcher")).hasSize(1);
        assertThat(findPermanent(player1, "Greenside Watcher").getCard().isToken()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Stolen Identity");
    }

    @Test
    void combatDamageCastsFreeCopyWithANewArtifactTargetAndDoesNotEncodeCopy() {
        Permanent attacker = addCreatureReady(player1, new GreensideWatcher());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new RazortipWhip());
        harness.setHand(player1, List.of(new StolenIdentity()));
        addStolenIdentityMana();

        harness.castAndResolveSorcery(player1, 0, List.of(attacker.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Greenside Watcher")).hasSize(2);
        assertThat(findPermanents(player1, "Razortip Whip")).hasSize(1);
        assertThat(findPermanent(player1, "Razortip Whip").getCard().isToken()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).hasSize(1);
        harness.assertNotInGraveyard(player1, "Stolen Identity");
    }

    @Test
    void combatDamageCopyCanBeDeclined() {
        Permanent attacker = addCreatureReady(player1, new GreensideWatcher());
        harness.setHand(player1, List.of(new StolenIdentity()));
        addStolenIdentityMana();

        harness.castAndResolveSorcery(player1, 0, List.of(attacker.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Greenside Watcher")).hasSize(2);
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void acceptingCipherWithoutACreatureLeavesArtifactCopyAndSpellInGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RazortipWhip());
        harness.setHand(player1, List.of(new StolenIdentity()));
        addStolenIdentityMana();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Razortip Whip")).hasSize(1);
        assertThat(findPermanent(player1, "Razortip Whip").getCard().isToken()).isTrue();
        harness.assertInGraveyard(player1, "Stolen Identity");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canCopyATokenWithoutCopyingItsTappedStatus() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreensideWatcher());
        harness.setHand(player1, List.of(new StolenIdentity(), new StolenIdentity()));
        addStolenIdentityMana();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, false);
        Permanent originalToken = findPermanent(player1, "Greenside Watcher");
        originalToken.tap();

        addStolenIdentityMana();
        harness.castAndResolveSorcery(player1, 0, List.of(originalToken.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Greenside Watcher")).hasSize(2);
        Permanent copiedToken = findPermanents(player1, "Greenside Watcher").stream()
                .filter(permanent -> !permanent.getId().equals(originalToken.getId()))
                .findFirst().orElseThrow();
        assertThat(copiedToken.getCard().isToken()).isTrue();
        assertThat(copiedToken.isTapped()).isFalse();
        assertThat(originalToken.isTapped()).isTrue();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void creatureCopyRetainsItsActivatedAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreensideWatcher());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());
        gate.tap();
        harness.setHand(player1, List.of(new StolenIdentity()));
        addStolenIdentityMana();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, false);
        Permanent token = findPermanent(player1, "Greenside Watcher");
        token.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, gate.getId());
        harness.passBothPriorities();

        assertThat(gate.isTapped()).isFalse();
        assertThat(token.isTapped()).isTrue();
    }

    @Test
    void losingAllAbilitiesAfterEncodingSuppressesCipherTrigger() {
        Permanent attacker = addCreatureReady(player1, new GreensideWatcher());
        harness.setHand(player1, List.of(new StolenIdentity(), new TurnToFrog()));
        addStolenIdentityMana();

        harness.castAndResolveSorcery(player1, 0, List.of(attacker.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(findPermanents(player1, "Greenside Watcher")).hasSize(2);
    }

    private void addStolenIdentityMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
