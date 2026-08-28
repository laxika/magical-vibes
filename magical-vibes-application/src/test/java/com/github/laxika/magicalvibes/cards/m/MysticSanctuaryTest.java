package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticSanctuary.class, Island.class, LightningBolt.class, GrizzlyBears.class})
class MysticSanctuaryTest extends BaseCardTest {

    @Test
    void entersTappedWithFewerThanThreeOtherIslands() {
        addIsland(player1);
        addIsland(player1);

        playSanctuary();

        assertThat(findSanctuary(player1).isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void entersUntappedAndOffersAnInstantOrSorceryFromTheGraveyardWithThreeOtherIslands() {
        Card instant = new LightningBolt();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(instant, creature));
        addIsland(player1);
        addIsland(player1);
        addIsland(player1);

        playSanctuary();

        assertThat(findSanctuary(player1).isTapped()).isFalse();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(instant.getId());

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(instant.getId());
        harness.assertNotInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    void triggerStillResolvesIfSanctuaryIsTappedAfterEnteringUntapped() {
        Card instant = new LightningBolt();
        harness.setGraveyard(player1, List.of(instant));
        addIsland(player1);
        addIsland(player1);
        addIsland(player1);

        playSanctuary();

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        int sanctuaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findSanctuary(player1));
        harness.activateAbility(player1, sanctuaryIndex, 0, null, null);

        assertThat(findSanctuary(player1).isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(instant.getId());
    }

    @Test
    void tapsForBlueMana() {
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player1, new MysticSanctuary());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(sanctuary.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private void playSanctuary() {
        harness.setHand(player1, List.of(new MysticSanctuary()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private void addIsland(Player player) {
        harness.addToBattlefield(player, new Island());
    }

    private Permanent findSanctuary(Player player) {
        return findPermanent(player, "Mystic Sanctuary");
    }
}
