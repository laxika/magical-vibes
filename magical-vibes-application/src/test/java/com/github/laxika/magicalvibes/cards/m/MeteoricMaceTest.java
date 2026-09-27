package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeteoricMace.class, GrizzlyBears.class, Forest.class, LlanowarElves.class})
class MeteoricMaceTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +4/+0 and trample")
    void equippedCreatureGetsBonusAndTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mace = addMaceReady(player1);
        mace.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equip {4} attaches Meteoric Mace to a creature you control")
    void equipAttachesMace() {
        Permanent mace = addMaceReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mace.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cascade skips lands and casts the first cheaper nonland card for free")
    void cascadeCastsFirstCheaperNonland() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        LlanowarElves hit = new LlanowarElves();
        Forest land = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(land, hit));
        harness.setHand(player1, List.of(new MeteoricMace()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class))
                .isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList())
                .containsExactly("Llanowar Elves");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Llanowar Elves"));
    }

    private Permanent addMaceReady(Player player) {
        Permanent permanent = new Permanent(new MeteoricMace());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

}
