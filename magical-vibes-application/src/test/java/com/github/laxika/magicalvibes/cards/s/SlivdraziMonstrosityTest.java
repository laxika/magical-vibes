package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HandOfEmrakul;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlivdraziMonstrosity.class, SinewSliver.class, HandOfEmrakul.class, GrizzlyBears.class})
class SlivdraziMonstrosityTest extends BaseCardTest {

    @Test
    @DisplayName("Eldrazi become Slivers, and Slivers gain devoid and annihilator 1")
    void grantsSliverAbilities() {
        Permanent monstrosity = addCreatureReady(player1, new SlivdraziMonstrosity());
        Permanent sinewSliver = addCreatureReady(player1, new SinewSliver());
        Permanent handOfEmrakul = addCreatureReady(player1, new HandOfEmrakul());
        addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectiveColors(gd, monstrosity)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, sinewSliver)).isEmpty();
        assertThat(gqs.getEffectivePower(gd, handOfEmrakul)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, sinewSliver, Keyword.DEVOID)).isTrue();

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(sinewSliver)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creates an Eldrazi Sliver token that sacrifices for colorless mana")
    void createsManaToken() {
        addCreatureReady(player1, new SlivdraziMonstrosity());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Eldrazi Sliver");
        assertThat(gqs.hasKeyword(gd, token, Keyword.DEVOID)).isTrue();

        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.activateAbility(player1, tokenIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Eldrazi Sliver");
    }
}
