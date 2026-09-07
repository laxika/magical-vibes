package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XuIfitOsteoharmonist.class, ElvishVisionary.class, GrizzlyBears.class,
        HolyDay.class, ZuranSpellcaster.class})
class XuIfitOsteoharmonistTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature as a Skeleton without its abilities")
    void returnsCreatureAsSkeletonWithoutAbilities() {
        Card creature = new ZuranSpellcaster();
        addReadyXuIfit();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Zuran Spellcaster");
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned)).contains(CardSubtype.SKELETON);
        returned.setSummoningSick(false);
        int returnedIndex = gd.playerBattlefields.get(player1.getId()).indexOf(returned);

        assertThatThrownBy(() -> harness.activateAbility(player1, returnedIndex, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Suppresses the returned creature's enter-the-battlefield ability")
    void suppressesReturnedCreatureEtbAbility() {
        Card creature = new ElvishVisionary();
        Card libraryCard = new GrizzlyBears();
        addReadyXuIfit();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryCard);
    }

    @Test
    @DisplayName("Can target only a creature card in the controller's graveyard")
    void targetsOnlyCreatureCards() {
        addReadyXuIfit();
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonCreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only at sorcery speed")
    void canActivateOnlyAtSorcerySpeed() {
        addReadyXuIfit();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyXuIfit() {
        Permanent xuIfit = harness.addToBattlefieldAndReturn(player1, new XuIfitOsteoharmonist());
        xuIfit.setSummoningSick(false);
    }
}
