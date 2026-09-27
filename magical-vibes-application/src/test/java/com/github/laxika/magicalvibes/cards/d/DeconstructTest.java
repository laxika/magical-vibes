package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.t.TelJiladChosen;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Deconstruct.class, AetherSpellbomb.class, TelJiladChosen.class, YotianSoldier.class})
class DeconstructTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Deconstruct destroys an artifact and adds three green mana")
    void destroysArtifactAndAddsMana() {
        harness.addToBattlefield(player2, new AetherSpellbomb());
        harness.setHand(player1, List.of(new Deconstruct()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Aether Spellbomb");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Aether Spellbomb");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Deconstruct can target an artifact creature")
    void destroysArtifactCreature() {
        harness.addToBattlefield(player2, new YotianSoldier());
        harness.setHand(player1, List.of(new Deconstruct()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Yotian Soldier");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Yotian Soldier");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Deconstruct cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new TelJiladChosen());
        harness.setHand(player1, List.of(new Deconstruct()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID creatureId = harness.getPermanentId(player2, "Tel-Jilad Chosen");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Deconstruct fizzles when its target leaves before resolution")
    void fizzlesWhenTargetLeaves() {
        harness.addToBattlefield(player2, new AetherSpellbomb());
        harness.setHand(player1, List.of(new Deconstruct()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Aether Spellbomb");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertInGraveyard(player1, "Deconstruct");
    }
}
