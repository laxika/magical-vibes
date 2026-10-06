package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerraSGuardian.class, Vorstclaw.class})
class SerraSGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control gain vigilance")
    void grantsVigilanceToOtherOwnCreatures() {
        Permanent creature = addCreatureReady(player1, new Vorstclaw());
        addCreatureReady(player1, new SerraSGuardian());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant vigilance to an opponent's creatures")
    void doesNotGrantVigilanceToOpponentsCreatures() {
        Permanent opponentCreature = addCreatureReady(player2, new Vorstclaw());
        addCreatureReady(player1, new SerraSGuardian());

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The vigilance grant ends when Serra's Guardian leaves the battlefield")
    void removesVigilanceWhenGuardianLeaves() {
        Permanent creature = addCreatureReady(player1, new Vorstclaw());
        Permanent guardian = addCreatureReady(player1, new SerraSGuardian());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(guardian);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creatures that enter after Serra's Guardian also gain vigilance")
    void grantsVigilanceToCreaturesEnteringLater() {
        addCreatureReady(player1, new SerraSGuardian());
        Permanent creature = addCreatureReady(player1, new Vorstclaw());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Serra's Guardian and its other creatures do not tap to attack")
    void attackingDoesNotTapGuardianOrOtherCreatures() {
        Permanent guardian = addCreatureReady(player1, new SerraSGuardian());
        Permanent creature = addCreatureReady(player1, new Vorstclaw());

        declareAttackers(List.of(0, 1));

        assertThat(guardian.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The remaining Guardian continues granting vigilance after another leaves")
    void remainingGuardianKeepsGrantingVigilance() {
        Permanent creature = addCreatureReady(player1, new Vorstclaw());
        Permanent firstGuardian = addCreatureReady(player1, new SerraSGuardian());
        Permanent secondGuardian = addCreatureReady(player1, new SerraSGuardian());

        gd.playerBattlefields.get(player1.getId()).remove(firstGuardian);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(secondGuardian);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }
}
