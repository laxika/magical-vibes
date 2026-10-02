package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.ChronicFlooding;
import com.github.laxika.magicalvibes.cards.c.Cobblebrute;
import com.github.laxika.magicalvibes.cards.d.DeadReveler;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbruptDecay.class, Cancel.class, DeadReveler.class, DrudgeBeetle.class,
        Cobblebrute.class, Island.class, ChronicFlooding.class, PithingNeedle.class})
class AbruptDecayTest extends BaseCardTest {

    private void giveManaAndCard(AbruptDecay card) {
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Test
    @DisplayName("Destroys target nonland permanent with mana value 3 or less")
    void destroysLowManaValuePermanent() {
        // Dead Reveler is {2}{B} — mana value exactly 3 (boundary).
        harness.addToBattlefield(player2, new DeadReveler());
        UUID targetId = harness.getPermanentId(player2, "Dead Reveler");
        giveManaAndCard(new AbruptDecay());

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Dead Reveler");
        harness.assertInGraveyard(player2, "Dead Reveler");
    }

    @Test
    @DisplayName("Cannot target a permanent with mana value greater than 3")
    void cannotTargetHighManaValue() {
        // Cobblebrute is {3}{R} — mana value 4.
        harness.addToBattlefield(player2, new Cobblebrute());
        harness.addToBattlefield(player1, new DrudgeBeetle());
        UUID targetId = harness.getPermanentId(player2, "Cobblebrute");
        giveManaAndCard(new AbruptDecay());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        harness.addToBattlefield(player2, new Island());
        UUID landId = harness.getPermanentId(player2, "Island");
        giveManaAndCard(new AbruptDecay());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Can't be countered — Cancel resolves but Abrupt Decay still destroys")
    void cannotBeCountered() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        UUID targetId = harness.getPermanentId(player2, "Drudge Beetle");
        AbruptDecay decay = new AbruptDecay();
        giveManaAndCard(decay);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, decay.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Drudge Beetle");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Destroys a noncreature artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new PithingNeedle());
        UUID targetId = harness.getPermanentId(player2, "Pithing Needle");
        giveManaAndCard(new AbruptDecay());

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Pithing Needle");
        harness.assertInGraveyard(player2, "Pithing Needle");
    }

    @Test
    @DisplayName("Destroys an enchantment attached to a land without destroying the land")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new Island());
        UUID landId = harness.getPermanentId(player2, "Island");
        var aura = harness.addToBattlefieldAndReturn(player2, new ChronicFlooding());
        aura.setAttachedTo(landId);
        giveManaAndCard(new AbruptDecay());

        harness.castAndResolveInstant(player1, 0, aura.getId());

        harness.assertNotOnBattlefield(player2, "Chronic Flooding");
        harness.assertInGraveyard(player2, "Chronic Flooding");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Can destroy a permanent controlled by its caster")
    void destroysOwnPermanent() {
        harness.addToBattlefield(player1, new DrudgeBeetle());
        UUID targetId = harness.getPermanentId(player1, "Drudge Beetle");
        giveManaAndCard(new AbruptDecay());

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Drudge Beetle");
        harness.assertInGraveyard(player1, "Drudge Beetle");
    }

    @Test
    @DisplayName("Still fails to resolve when its only target leaves the battlefield")
    void doesNotResolveWithMissingTarget() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        UUID targetId = harness.getPermanentId(player2, "Drudge Beetle");
        giveManaAndCard(new AbruptDecay());
        harness.setHand(player2, List.of(new AbruptDecay()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.assertInGraveyard(player2, "Drudge Beetle");
        harness.assertNotInGraveyard(player1, "Abrupt Decay");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Abrupt Decay");
        harness.assertInGraveyard(player2, "Abrupt Decay");
        assertThat(gd.stack).isEmpty();
    }
}
