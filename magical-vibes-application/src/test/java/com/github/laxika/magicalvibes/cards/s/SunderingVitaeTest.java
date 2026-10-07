package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.cards.g.GolgariBrownscale;
import com.github.laxika.magicalvibes.cards.g.GolgariGermination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunderingVitae.class, BorosSignet.class, GolgariGermination.class,
        BorosRecruit.class, GlassGolem.class, GolgariBrownscale.class})
class SunderingVitaeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new BorosSignet());
        castAndResolveAt(harness.getPermanentId(player2, "Boros Signet"));

        harness.assertNotOnBattlefield(player2, "Boros Signet");
        harness.assertInGraveyard(player2, "Boros Signet");
    }

    @Test
    @DisplayName("Destroys a target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new GolgariGermination());
        castAndResolveAt(harness.getPermanentId(player2, "Golgari Germination"));

        harness.assertNotOnBattlefield(player2, "Golgari Germination");
        harness.assertInGraveyard(player2, "Golgari Germination");
    }

    @Test
    @DisplayName("Destroys a target artifact creature")
    void destroysArtifactCreature() {
        harness.addToBattlefield(player2, new GlassGolem());
        castAndResolveAt(harness.getPermanentId(player2, "Glass Golem"));

        harness.assertNotOnBattlefield(player2, "Glass Golem");
        harness.assertInGraveyard(player2, "Glass Golem");
    }

    @Test
    @DisplayName("Convoke taps creatures to help pay the generic cost")
    void convokePaysForTheSpell() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        harness.addToBattlefield(player2, new BorosSignet());
        harness.setHand(player1, List.of(new SunderingVitae()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Boros Signet");
        harness.castInstantWithConvoke(player1, 0, List.of(targetId),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Boros Signet");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new BorosRecruit());
        harness.setHand(player1, List.of(new SunderingVitae()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID creatureId = harness.getPermanentId(player2, "Boros Recruit");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy an artifact you control")
    void destroysOwnArtifact() {
        harness.addToBattlefield(player1, new BorosSignet());
        castAndResolveAt(harness.getPermanentId(player1, "Boros Signet"));

        harness.assertNotOnBattlefield(player1, "Boros Signet");
        harness.assertInGraveyard(player1, "Boros Signet");
    }

    @Test
    @DisplayName("Green and colorless creatures can convoke the entire spell, even with summoning sickness")
    void convokePaysEntireCost() {
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new GolgariBrownscale());
        Permanent firstGolem = harness.addToBattlefieldAndReturn(player1, new GlassGolem());
        Permanent secondGolem = harness.addToBattlefieldAndReturn(player1, new GlassGolem());
        greenCreature.setSummoningSick(true);
        firstGolem.setSummoningSick(true);
        secondGolem.setSummoningSick(true);
        harness.addToBattlefield(player2, new BorosSignet());
        harness.setHand(player1, List.of(new SunderingVitae()));

        harness.castInstantWithConvoke(player1, 0,
                List.of(harness.getPermanentId(player2, "Boros Signet")),
                List.of(firstGolem.getId(), greenCreature.getId(), secondGolem.getId()));

        assertThat(greenCreature.isTapped()).isTrue();
        assertThat(firstGolem.isTapped()).isTrue();
        assertThat(secondGolem.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Boros Signet");
        harness.assertInGraveyard(player1, "Sundering Vitae");
    }

    @Test
    @DisplayName("Nongreen creatures cannot convoke the green mana requirement")
    void convokeCannotPayGreenWithNongreenCreatures() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent thirdCreature = harness.addToBattlefieldAndReturn(player1, new GlassGolem());
        harness.addToBattlefield(player2, new BorosSignet());
        harness.setHand(player1, List.of(new SunderingVitae()));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(harness.getPermanentId(player2, "Boros Signet")),
                List.of(firstCreature.getId(), secondCreature.getId(), thirdCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Boros Signet");
        harness.assertInHand(player1, "Sundering Vitae");
    }

    private void castAndResolveAt(UUID targetId) {
        harness.setHand(player1, List.of(new SunderingVitae()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
