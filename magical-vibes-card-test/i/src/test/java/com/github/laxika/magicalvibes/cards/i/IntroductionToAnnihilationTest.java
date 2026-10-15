package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.s.SpinedKarok;
import com.github.laxika.magicalvibes.cards.t.TemptedByTheOriq;
import com.github.laxika.magicalvibes.cards.w.WitherbloomCampus;
import com.github.laxika.magicalvibes.cards.z.ZephyrBoots;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntroductionToAnnihilation.class, SpinedKarok.class, WitherbloomCampus.class,
        TemptedByTheOriq.class, ZephyrBoots.class})
class IntroductionToAnnihilationTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target nonland permanent and its controller draws a card")
    void exilesTargetNonlandPermanentAndItsControllerDraws() {
        harness.addToBattlefield(player2, new SpinedKarok());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new SpinedKarok(), new SpinedKarok()));
        UUID targetId = harness.getPermanentId(player2, "Spined Karok");

        prepareIntroduction();
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Spined Karok");
        harness.assertNotInGraveyard(player2, "Spined Karok");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Spined Karok"));
        harness.assertInHand(player2, "Spined Karok");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new WitherbloomCampus());
        UUID targetId = harness.getPermanentId(player2, "Witherbloom Campus");

        prepareIntroduction();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Does not draw when the target leaves before resolution")
    void fizzlesWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new SpinedKarok());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new SpinedKarok()));
        UUID targetId = harness.getPermanentId(player2, "Spined Karok");

        prepareIntroduction();
        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can exile your own permanent and draw exactly one card")
    void exilesOwnPermanentAndDraws() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new SpinedKarok()).getId();
        harness.setLibrary(player1, List.of(new SpinedKarok(), new SpinedKarok()));
        harness.setHand(player2, List.of());

        prepareIntroduction();
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Spined Karok");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Spined Karok");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can exile a noncreature artifact and its controller draws")
    void exilesNoncreaturePermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new ZephyrBoots()).getId();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new SpinedKarok()));

        prepareIntroduction();
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Zephyr Boots");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Zephyr Boots"));
        harness.assertInHand(player2, "Spined Karok");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A stolen permanent's controller draws while its owner receives the exiled card")
    void controllerDrawsRatherThanOwner() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SpinedKarok()).getId();
        harness.setHand(player1, List.of(new TemptedByTheOriq()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, List.of(targetId));
        harness.assertOnBattlefield(player1, "Spined Karok");
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new SpinedKarok()));
        harness.setLibrary(player2, List.of(new SpinedKarok()));

        prepareIntroduction();
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Spined Karok");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Spined Karok"));
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Spined Karok");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    private void prepareIntroduction() {
        harness.setHand(player1, List.of(new IntroductionToAnnihilation()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
