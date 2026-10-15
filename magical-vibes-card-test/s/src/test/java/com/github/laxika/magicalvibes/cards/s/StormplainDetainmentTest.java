package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormplainDetainment.class, Forest.class, GrizzlyBears.class, Naturalize.class, Pacifism.class})
class StormplainDetainmentTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles target nonland permanent an opponent controls until source leaves")
    void exilesTargetUntilSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolve(target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        Permanent source = findPermanent(player1, "Stormplain Detainment");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, source.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by its controller")
    void cannotTargetOwnPermanent() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing the source before its enters trigger resolves prevents exile")
    void sourceLeavesBeforeTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        UUID sourceId = harness.getPermanentId(player1, "Stormplain Detainment");
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, sourceId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stormplain Detainment");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can exile an opposing noncreature enchantment")
    void exilesOpponentEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StormplainDetainment());
        castAndResolve(target.getId());

        harness.assertOnBattlefield(player1, "Stormplain Detainment");
        harness.assertNotOnBattlefield(player2, "Stormplain Detainment");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Can resolve without any legal enters-trigger targets")
    void resolvesWithoutLegalTargets() {
        harness.addToBattlefield(player2, new Forest());
        prepareCast();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stormplain Detainment");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The owner chooses a new attachment when an exiled Aura returns")
    void returningAuraChoosesAttachment() {
        Permanent originalHost = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent newHost = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(originalHost.getId());
        castAndResolve(aura.getId());
        harness.assertNotOnBattlefield(player2, "Pacifism");

        UUID sourceId = harness.getPermanentId(player1, "Stormplain Detainment");
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, sourceId);
        harness.handlePermanentChosen(player2, newHost.getId());

        harness.assertOnBattlefield(player2, "Pacifism");
        assertThat(findPermanent(player2, "Pacifism").getAttachedTo()).isEqualTo(newHost.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Pacifism"));
    }

    @Test
    @DisplayName("An exiled Aura remains in exile when nothing can legally be enchanted")
    void returningAuraWithoutLegalAttachmentStaysExiled() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(host.getId());
        castAndResolve(aura.getId());
        UUID firstSourceId = harness.getPermanentId(player1, "Stormplain Detainment");
        castAndResolve(host.getId());

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, firstSourceId);

        harness.assertNotOnBattlefield(player2, "Pacifism");
        harness.assertNotInGraveyard(player2, "Pacifism");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(aura.getCard().getId()));
    }

    @Test
    @DisplayName("The exile trigger does nothing if its target leaves before resolution")
    void targetLeavesBeforeTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StormplainDetainment());
        prepareCast();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stormplain Detainment");
        harness.assertInGraveyard(player2, "Stormplain Detainment");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An exiled creature returns as a new permanent without its old counters")
    void returnedCreatureLosesCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        castAndResolve(target.getId());

        UUID sourceId = harness.getPermanentId(player1, "Stormplain Detainment");
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, sourceId);

        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void castAndResolve(UUID targetId) {
        prepareCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StormplainDetainment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
