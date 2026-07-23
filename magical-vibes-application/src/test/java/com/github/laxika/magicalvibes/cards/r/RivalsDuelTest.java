package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RivalsDuelTest extends BaseCardTest {

    @Test
    @DisplayName("Two creatures sharing no creature types fight each other")
    void creaturesSharingNoTypesFight() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RivalsDuel()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castSorcery(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        // Bear (2/2) deals 2 to Elf (1/1) which dies; Elf's 1 damage leaves the Bear alive.
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spell does not resolve when both targets gain shroud")
    void doesNotResolveWhenBothTargetsGainShroud() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RivalsDuel()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent elves = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.castSorcery(player1, 0, List.of(bears.getId(), elves.getId()));

        bears.getGrantedKeywords().add(Keyword.SHROUD);
        elves.getGrantedKeywords().add(Keyword.SHROUD);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(elves.getMarkedDamage()).isZero();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles (illegal target)"));
    }

    @Test
    @DisplayName("Neither creature fights when one target gains shroud")
    void creaturesDoNotFightWhenOneTargetGainsShroud() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RivalsDuel()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent elves = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.castSorcery(player1, 0, List.of(bears.getId(), elves.getId()));

        elves.getGrantedKeywords().add(Keyword.SHROUD);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(elves.getMarkedDamage()).isZero();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("fizzles (illegal target)"));
    }

    @Test
    @DisplayName("Cannot choose two creatures that share a creature type")
    void cannotTargetCreaturesSharingType() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RivalsDuel()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID firstBear = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondBear = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(firstBear, secondBear)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature types");
    }

    @Test
    @DisplayName("A Changeling shares every creature type, so it cannot be paired with another creature")
    void cannotTargetChangelingWithAnyCreature() {
        harness.addToBattlefield(player1, new AvianChangeling());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RivalsDuel()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID changelingId = harness.getPermanentId(player1, "Avian Changeling");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(changelingId, elvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature types");
    }
}
