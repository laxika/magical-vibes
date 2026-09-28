package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinLancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SidarJabariOfZhalfir.class, Forest.class, GrizzlyBears.class, MindStone.class,
        ZhalfirinLancer.class})
class SidarJabariOfZhalfirTest extends BaseCardTest {

    @Test
    void eminenceLootsWhenAKnightAttacksFromTheBattlefield() {
        addCreatureReady(player1, new SidarJabariOfZhalfir());
        harness.setHand(player1, List.of(new MindStone()));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Mind Stone");
    }

    @Test
    void eminenceWorksFromTheCommandZone() {
        Card sidar = new SidarJabariOfZhalfir();
        gd.playerCommandZones.get(player1.getId()).add(sidar);
        addCreatureReady(player1, new ZhalfirinLancer());
        harness.setHand(player1, List.of(new MindStone()));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Mind Stone");
    }

    @Test
    void combatDamageReturnsOnlyATargetedKnightFromTheGraveyard() {
        Card knight = new ZhalfirinLancer();
        Card nonKnight = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonKnight, knight));

        Permanent sidar = addCreatureReady(player1, new SidarJabariOfZhalfir());
        sidar.setAttacking(true);
        resolveCombat();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(knight.getId());

        harness.handleMultipleCardsChosen(player1, List.of(knight.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(knight.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonKnight);
    }
}
