package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CodieVociferousCodex.class, DarkRitual.class, Divination.class, GrizzlyBears.class, Shock.class})
class CodieVociferousCodexTest extends BaseCardTest {

    @Test
    @DisplayName("Codie prevents its controller from casting permanent spells")
    void preventsPermanentSpells() {
        addCodie();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Codie's ability adds one mana of each color")
    void addsOneManaOfEachColor() {
        Permanent codie = addCodie();
        activateCodie(codie);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(codie.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Codie's delayed cascade still triggers after Codie leaves the battlefield")
    void delayedCascadeSurvivesSourceLeaving() {
        Permanent codie = addCodie();
        activateCodie(codie);
        gd.playerBattlefields.get(player1.getId()).remove(codie);

        harness.setHand(player1, List.of(new Divination()));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new DarkRitual());
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isInstanceOf(
                PendingInteraction.LibrarySearch.class);
    }

    @Test
    @DisplayName("Codie cascades from the next instant or sorcery using that spell's mana value")
    void cascadesFromNextSpell() {
        Permanent codie = addCodie();
        activateCodie(codie);

        harness.setHand(player1, List.of(new DarkRitual()));
        gd.playerDecks.get(player1.getId()).clear();
        Shock shock = new Shock();
        gd.playerDecks.get(player1.getId()).addAll(List.of(shock));
        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("Codie offers a lesser instant or sorcery for free and only triggers once")
    void offersLesserInstantOrSorceryOnce() {
        Permanent codie = addCodie();
        activateCodie(codie);

        harness.setHand(player1, List.of(new Divination()));
        DarkRitual hit = new DarkRitual();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(hit));
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Dark Ritual");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(hit);
    }

    private Permanent addCodie() {
        return addCreatureReady(player1, new CodieVociferousCodex());
    }

    private void activateCodie(Permanent codie) {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(codie), null, null);
        harness.clearPriorityPassed();
    }
}
