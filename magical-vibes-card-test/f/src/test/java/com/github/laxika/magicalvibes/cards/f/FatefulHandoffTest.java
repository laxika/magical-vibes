package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.r.RocHunter;
import com.github.laxika.magicalvibes.cards.r.RustGoliath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FatefulHandoff.class, RocHunter.class, EnergyRefractor.class, Forest.class,
        RustGoliath.class, GoForTheThroat.class})
class FatefulHandoffTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to a creature's mana value, then gives it to the chosen opponent")
    void drawsAndGivesAwayCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new RocHunter()).getId();
        harness.setHand(player1, List.of(new FatefulHandoff()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getId().equals(targetId));
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(permanent -> permanent.getId().equals(targetId));
    }

    @Test
    @DisplayName("Can target an artifact you control")
    void canTargetArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor()).getId();
        harness.setHand(player1, List.of(new FatefulHandoff()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(permanent -> permanent.getId().equals(targetId));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Forest()).getId();
        harness.setHand(player1, List.of(new FatefulHandoff()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new RocHunter()).getId();
        harness.setHand(player1, List.of(new FatefulHandoff()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose yourself as an additional player target to keep the permanent")
    void cannotKeepPermanentByTargetingYourself() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new RocHunter()).getId();
        harness.setHand(player1, List.of(new FatefulHandoff()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player1.getId(), targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Draws five cards for a prototyped Rust Goliath")
    void drawsUsingPrototypeManaValue() {
        harness.setHand(player1, List.of(new RustGoliath()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();
        UUID targetId = harness.getPermanentId(player1, "Rust Goliath");
        harness.setHand(player1, List.of(new FatefulHandoff()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(permanent -> permanent.getId().equals(targetId));
    }

    @Test
    @DisplayName("Does not draw when the only target is destroyed before resolution")
    void doesNotDrawWhenTargetLeavesBattlefield() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new RocHunter()).getId();
        harness.setHand(player1, List.of(new FatefulHandoff()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Roc Hunter");
        harness.assertNotOnBattlefield(player2, "Roc Hunter");
    }
}
