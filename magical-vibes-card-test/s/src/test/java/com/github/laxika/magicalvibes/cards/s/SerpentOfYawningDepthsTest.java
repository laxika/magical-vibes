package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantOctopus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerpentOfYawningDepths.class, GrizzlyBears.class, KrakenHatchling.class,
        SegovianLeviathan.class, GiantOctopus.class, SerpentWarrior.class})
class SerpentOfYawningDepthsTest extends BaseCardTest {

    @Test
    @DisplayName("Sea monsters you control can only be blocked by sea monsters")
    void seaMonstersCanOnlyBeBlockedBySeaMonsters() {
        addCreatureReady(player1, new SerpentOfYawningDepths());
        addCreatureReady(player1, new KrakenHatchling());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Krakens, Leviathans, Octopuses, and Serpents");
    }

    @Test
    @DisplayName("Sea monsters can block sea monsters")
    void seaMonstersCanBlockSeaMonsters() {
        addCreatureReady(player1, new SerpentOfYawningDepths());
        addCreatureReady(player1, new KrakenHatchling());
        Permanent blocker = addCreatureReady(player2, new GiantOctopus());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = CardSubtype.class, names = {"KRAKEN", "LEVIATHAN", "OCTOPUS", "SERPENT"})
    @DisplayName("The restriction applies to all four supported creature types")
    void allSupportedCreatureTypesAreRestricted(CardSubtype subtype) {
        addCreatureReady(player1, new SerpentOfYawningDepths());
        addCreatureReady(player1, seaMonster(subtype));
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Krakens, Leviathans, Octopuses, and Serpents");
    }

    @Test
    @DisplayName("Non-sea-monsters you control are unaffected")
    void nonSeaMonstersAreUnaffected() {
        addCreatureReady(player1, new SerpentOfYawningDepths());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction does not affect sea monsters controlled by opponents")
    void opponentSeaMonstersAreUnaffected() {
        addCreatureReady(player1, new SerpentOfYawningDepths());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new KrakenHatchling());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = CardSubtype.class, names = {"KRAKEN", "LEVIATHAN", "OCTOPUS", "SERPENT"})
    @DisplayName("Each sea monster type can block the Serpent itself")
    void eachSeaMonsterTypeCanBlockTheSource(CardSubtype subtype) {
        addCreatureReady(player1, new SerpentOfYawningDepths());
        Permanent blocker = addCreatureReady(player2, seaMonster(subtype));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The Serpent itself cannot be blocked by an ordinary creature")
    void sourceIsAlsoRestricted() {
        addCreatureReady(player1, new SerpentOfYawningDepths());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Krakens, Leviathans, Octopuses, and Serpents");
    }

    @Test
    @DisplayName("Snakes are not Serpents and receive no restriction")
    void snakesAreUnaffected() {
        addCreatureReady(player1, new SerpentOfYawningDepths());
        addCreatureReady(player1, new SerpentWarrior());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A Snake cannot block a restricted Serpent")
    void snakesCannotBlockSeaMonsters() {
        addCreatureReady(player1, new SerpentOfYawningDepths());
        addCreatureReady(player2, new SerpentWarrior());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Krakens, Leviathans, Octopuses, and Serpents");
    }

    @Test
    @DisplayName("The restriction ends when the source leaves the battlefield")
    void restrictionEndsWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new SerpentOfYawningDepths());
        addCreatureReady(player1, new KrakenHatchling());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Card seaMonster(CardSubtype subtype) {
        return switch (subtype) {
            case KRAKEN -> new KrakenHatchling();
            case LEVIATHAN -> new SegovianLeviathan();
            case OCTOPUS -> new GiantOctopus();
            case SERPENT -> new SerpentOfYawningDepths();
            default -> throw new IllegalArgumentException("Not a sea monster: " + subtype);
        };
    }
}
