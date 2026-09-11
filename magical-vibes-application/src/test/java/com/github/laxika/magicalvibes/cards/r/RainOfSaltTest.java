package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RainOfSalt.class, ArgothianSwine.class, Forest.class, Mountain.class})
class RainOfSaltTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys two target lands")
    void destroysTwoTargetLands() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RainOfSalt()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.castAndResolveSorcery(player1, 0, List.of(mountainId, forestId));

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Still destroys the remaining land when one target is removed before resolution")
    void destroysRemainingWhenOneTargetRemoved() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RainOfSalt()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, List.of(mountainId, forestId));

        // Remove only one target before resolution
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Mountain"));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new ArgothianSwine());
        harness.setHand(player1, List.of(new RainOfSalt()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Argothian Swine");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(mountainId, creatureId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the same land twice")
    void cannotTargetSameLandTwice() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RainOfSalt()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID mountainId = harness.getPermanentId(player2, "Mountain");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(mountainId, mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
