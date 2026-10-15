package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpellSatchel.class, BarkshellBlessing.class, Forest.class, GrizzlyBears.class,
        LightningBolt.class, SecretRendezvous.class})
class SpellSatchelTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant puts a book counter on Spell Satchel")
    void castingInstantAddsBookCounter() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(satchel.getCounterCount(CounterType.BOOK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Copying an instant puts another book counter on Spell Satchel")
    void copyingInstantAddsBookCounter() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        Permanent conspireA = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent conspireB = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(),
                List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(satchel.getCounterCount(CounterType.BOOK)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing a book counter adds a colorless mana")
    void removingBookCounterAddsColorlessMana() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        satchel.setCounterCount(CounterType.BOOK, 1);
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(satchel.getCounterCount(CounterType.BOOK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS))
                .isEqualTo(manaBefore + 1);
    }

    @Test
    @DisplayName("Removing three book counters draws a card")
    void removingThreeBookCountersDrawsCard() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        satchel.setCounterCount(CounterType.BOOK, 3);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(satchel.getCounterCount(CounterType.BOOK)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("The draw ability requires three book counters")
    void drawAbilityRequiresThreeBookCounters() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        satchel.setCounterCount(CounterType.BOOK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting a sorcery triggers before the spell resolves")
    void castingSorceryAddsBookCounterBeforeResolution() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        harness.setHand(player1, List.of(new SecretRendezvous()));
        harness.setLibrary(player1, List.of(new SpellSatchel(), new SpellSatchel(), new SpellSatchel()));
        harness.setLibrary(player2, List.of(new SpellSatchel(), new SpellSatchel(), new SpellSatchel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(satchel.getCounterCount(CounterType.BOOK)).isZero();
        harness.passBothPriorities();
        assertThat(satchel.getCounterCount(CounterType.BOOK)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(satchel.getCounterCount(CounterType.BOOK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting an artifact does not trigger magecraft")
    void castingArtifactDoesNotAddBookCounter() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        harness.setHand(player1, List.of(new SpellSatchel()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(satchel.getCounterCount(CounterType.BOOK)).isZero();
    }

    @Test
    @DisplayName("An opponent's instant does not trigger magecraft")
    void opponentCastingInstantDoesNotAddBookCounter() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(satchel.getCounterCount(CounterType.BOOK)).isZero();
    }

    @Test
    @DisplayName("The mana ability requires a book counter")
    void manaAbilityRequiresBookCounter() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(satchel.isTapped()).isFalse();
        assertThat(satchel.getCounterCount(CounterType.BOOK)).isZero();
    }

    @Test
    @DisplayName("The mana ability resolves immediately and taps its source")
    void manaAbilityResolvesImmediatelyAndRequiresUntappedSource() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        satchel.setCounterCount(CounterType.BOOK, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(satchel.isTapped()).isTrue();
        assertThat(satchel.getCounterCount(CounterType.BOOK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(satchel.getCounterCount(CounterType.BOOK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Drawing pays counters and mana immediately but waits for resolution")
    void drawAbilityPaysCostsBeforeResolution() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        satchel.setCounterCount(CounterType.BOOK, 4);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SpellSatchel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(satchel.isTapped()).isTrue();
        assertThat(satchel.getCounterCount(CounterType.BOOK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The draw ability cannot be activated without three mana")
    void drawAbilityRequiresMana() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        satchel.setCounterCount(CounterType.BOOK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(satchel.isTapped()).isFalse();
        assertThat(satchel.getCounterCount(CounterType.BOOK)).isEqualTo(3);
    }
}
