package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoundryHelix.class, GrizzlyBears.class, Spellbook.class, Ornithopter.class})
class FoundryHelixTest extends BaseCardTest {

    @Test
    void sacrificesAnArtifactDealsDamageAndGainsLife() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FoundryHelix()));
        addMana();

        harness.castInstantWithSacrifice(player1, 0, target.getId(), artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 24);
    }

    @Test
    void sacrificesANonartifactPermanentWithoutGainingLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FoundryHelix()));
        addMana();

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    void sacrificingAnArtifactCreatureGainsLifeAndPaysCostImmediately() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new FoundryHelix()));
        addMana();

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), artifact.getId());

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    void sacrificingTheTargetPreventsDamageAndLifeGain() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new FoundryHelix()));
        addMana();

        harness.castInstantWithSacrifice(player1, 0, artifact.getId(), artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Foundry Helix");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void canTargetItsControllerAndGainLifeAfterDamage() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new FoundryHelix()));
        addMana();

        harness.castInstantWithSacrifice(player1, 0, player1.getId(), artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Foundry Helix");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotCastWithoutSacrificingAPermanent() {
        harness.setHand(player1, List.of(new FoundryHelix()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotSacrificeAnOpponentsPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new FoundryHelix()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
