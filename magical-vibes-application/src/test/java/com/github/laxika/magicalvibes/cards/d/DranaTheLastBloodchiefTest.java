package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DranaTheLastBloodchief.class, GrizzlyBears.class, Island.class})
class DranaTheLastBloodchiefTest extends BaseCardTest {

    @Test
    @DisplayName("The defending player chooses a nonlegendary creature to return with a counter and Vampire subtype")
    void defendingPlayerChoosesCreatureToReturn() {
        Card firstBear = new GrizzlyBears();
        Card chosenBear = new GrizzlyBears();
        Card land = new Island();
        Card legendaryCreature = new DranaTheLastBloodchief();
        Permanent drana = addCreatureReady(player1, new DranaTheLastBloodchief());
        harness.setGraveyard(player1, List.of(firstBear, chosenBear, land, legendaryCreature));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(drana)));
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).containsExactly(firstBear, chosenBear);

        harness.handleGraveyardCardChosen(player2, choice.cardPool().indexOf(chosenBear));

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(chosenBear.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(GameQueryService.permanentHasSubtype(returned, CardSubtype.VAMPIRE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstBear, land, legendaryCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger ignores noncreature and legendary creature cards")
    void ignoresIneligibleGraveyardCards() {
        Card land = new Island();
        Card legendaryCreature = new DranaTheLastBloodchief();
        Permanent drana = addCreatureReady(player1, new DranaTheLastBloodchief());
        harness.setGraveyard(player1, List.of(land, legendaryCreature));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(drana)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land, legendaryCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(legendaryCreature.getId()));
    }
}
