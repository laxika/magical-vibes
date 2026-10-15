package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.v.VampireLacerator;
import com.github.laxika.magicalvibes.cards.t.TerraStomper;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeastOfBlood.class, VampireLacerator.class, TerraStomper.class})
class FeastOfBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and gains 4 life with two Vampires")
    void destroysTargetCreatureAndGainsLife() {
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.addToBattlefield(player2, new TerraStomper());
        harness.setHand(player1, List.of(new FeastOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Terra Stomper");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Terra Stomper");
        harness.assertInGraveyard(player2, "Terra Stomper");
        harness.assertLife(player1, 24);
        harness.assertInGraveyard(player1, "Feast of Blood");
    }

    @Test
    @DisplayName("Cannot be cast without controlling two Vampires")
    void cannotCastWithoutTwoVampires() {
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.addToBattlefield(player2, new TerraStomper());
        harness.setHand(player1, List.of(new FeastOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Terra Stomper");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent's Vampires do not satisfy the casting restriction")
    void opponentsVampiresDoNotCount() {
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.addToBattlefield(player2, new VampireLacerator());
        harness.addToBattlefield(player2, new VampireLacerator());
        harness.setHand(player1, List.of(new FeastOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Vampire Lacerator");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Feast of Blood");
    }

    @Test
    @DisplayName("Losing all Vampires after casting does not prevent resolution")
    void resolvesAfterVampiresLeave() {
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.addToBattlefield(player2, new TerraStomper());
        harness.setHand(player1, List.of(new FeastOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Terra Stomper");
        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Terra Stomper");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Feast of Blood");
    }

    @Test
    @DisplayName("Can destroy one of the Vampires satisfying the casting restriction")
    void canTargetOwnVampire() {
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.setHand(player1, List.of(new FeastOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Vampire Lacerator");
        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetId));
        harness.assertInGraveyard(player1, "Vampire Lacerator");
        harness.assertLife(player1, 24);
        harness.assertInGraveyard(player1, "Feast of Blood");
    }

    @Test
    @DisplayName("Does not gain life when the target is gone at resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.addToBattlefield(player2, new TerraStomper());
        harness.setHand(player1, List.of(new FeastOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Terra Stomper");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Feast of Blood");
    }
}
