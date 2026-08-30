package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AdventuringGear;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VenerableKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TournamentGrounds.class, VenerableKnight.class, AdventuringGear.class, GrizzlyBears.class})
class TournamentGroundsTest extends BaseCardTest {

    @Test
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new TournamentGrounds());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void restrictedManaCastsKnightSpell() {
        harness.addToBattlefield(player1, new TournamentGrounds());
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(((PendingInteraction.ColorChoice) gd.interaction.activeInteraction()).options())
                .containsExactly("RED", "WHITE", "BLACK");
        harness.handleListChoice(player1, "WHITE");

        harness.setHand(player1, List.of(new VenerableKnight()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Venerable Knight");
    }

    @Test
    void restrictedManaCastsEquipmentSpell() {
        harness.addToBattlefield(player1, new TournamentGrounds());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        harness.setHand(player1, List.of(new AdventuringGear()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Adventuring Gear");
    }

    @Test
    void restrictedManaCannotCastOtherSpell() {
        harness.addToBattlefield(player1, new TournamentGrounds());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLACK");

        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
