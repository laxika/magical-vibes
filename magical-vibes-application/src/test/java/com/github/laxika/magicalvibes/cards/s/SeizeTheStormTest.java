package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeizeTheStormTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a red Elemental with trample sized by GY instants/sorceries plus itself")
    void createsElementalSizedByGraveyard() {
        harness.setHand(player1, List.of(new SeizeTheStorm()));
        harness.setGraveyard(player1, List.of(new Opt(), new ThinkTwice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent elemental = findElemental();
        // Opt + Think Twice + Seize the Storm itself in GY = 3
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Empty board: after resolve, self in GY makes the Elemental a 1/1")
    void emptyStartsAsOneOneAfterResolve() {
        harness.setHand(player1, List.of(new SeizeTheStorm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent elemental = findElemental();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts flashback cards owned in exile toward P/T")
    void countsFlashbackCardsInExile() {
        harness.setHand(player1, List.of(new SeizeTheStorm()));
        harness.setExile(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent elemental = findElemental();
        // Seize itself in GY (1) + Think Twice with flashback in exile (1) = 2
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count opponent's flashback cards in exile")
    void ignoresOpponentExile() {
        harness.setHand(player1, List.of(new SeizeTheStorm()));
        harness.setExile(player2, List.of(new ThinkTwice(), new ThinkTwice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent elemental = findElemental();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when more instants/sorceries enter the graveyard")
    void powerToughnessUpdateDynamically() {
        harness.setHand(player1, List.of(new SeizeTheStorm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent elemental = findElemental();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new Opt());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flashback creates the Elemental and exiles Seize the Storm")
    void flashbackCreatesTokenAndExilesSelf() {
        harness.setGraveyard(player1, List.of(new SeizeTheStorm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        Permanent elemental = findElemental();
        // Self exiled with flashback = 1; GY empty of I/S
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Seize the Storm"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Seize the Storm"));
    }

    private Permanent findElemental() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Elemental".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
