package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DwynensElite;
import com.github.laxika.magicalvibes.cards.e.ElectrostaticBolt;
import com.github.laxika.magicalvibes.cards.g.GoblinStriker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElderfangRitualist.class, DwynensElite.class, GoblinStriker.class, ElectrostaticBolt.class})
class ElderfangRitualistTest extends BaseCardTest {

    private void destroyElderfangRitualist(Permanent ritualist) {
        harness.setHand(player1, List.of(new ElectrostaticBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, ritualist.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("When it dies, it returns another target Elf card from its controller's graveyard to hand")
    void returnsTargetElfCard() {
        Permanent ritualist = harness.addToBattlefieldAndReturn(player1, new ElderfangRitualist());
        Card elf = new DwynensElite();
        harness.setGraveyard(player1, List.of(elf));

        destroyElderfangRitualist(ritualist);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(elf.getId());

        harness.handleMultipleCardsChosen(player1, List.of(elf.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dwynen's Elite");
        harness.assertInGraveyard(player1, "Elderfang Ritualist");
    }

    @Test
    @DisplayName("The death trigger excludes Elderfang Ritualist itself")
    void excludesItselfFromTargets() {
        Permanent ritualist = harness.addToBattlefieldAndReturn(player1, new ElderfangRitualist());
        Card elf = new DwynensElite();
        harness.setGraveyard(player1, List.of(elf));

        destroyElderfangRitualist(ritualist);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(elf.getId());
        assertThat(choice.validCardIds()).doesNotContain(ritualist.getCard().getId());
    }

    @Test
    @DisplayName("The death trigger only targets another Elf card in its controller's graveyard")
    void requiresAnotherElfInOwnGraveyard() {
        Permanent ritualist = harness.addToBattlefieldAndReturn(player1, new ElderfangRitualist());
        Card nonElf = new GoblinStriker();
        Card opponentElf = new DwynensElite();
        harness.setGraveyard(player1, List.of(nonElf));
        harness.setGraveyard(player2, List.of(opponentElf));

        destroyElderfangRitualist(ritualist);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
