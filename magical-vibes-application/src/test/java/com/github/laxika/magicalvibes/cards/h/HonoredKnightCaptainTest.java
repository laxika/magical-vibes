package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StriderHarness;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HonoredKnightCaptain.class, GrizzlyBears.class, StriderHarness.class})
class HonoredKnightCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 1/1 white Human Soldier token")
    void etbCreatesHumanSoldierToken() {
        harness.setHand(player1, List.of(new HonoredKnightCaptain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing this creature puts a searched Equipment onto the battlefield")
    void sacrificeSearchesForEquipment() {
        Permanent source = addReadySource();
        harness.setLibrary(player1, List.of(new StriderHarness(), new GrizzlyBears()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        harness.assertInGraveyard(player1, "Honored Knight-Captain");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Strider Harness");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent equipment = findPermanent(player1, "Strider Harness");
        assertThat(equipment).isNotNull();
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The sacrifice ability resolves without an Equipment in the library")
    void sacrificeSearchFindsNoEquipment() {
        addReadySource();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Honored Knight-Captain");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
    }

    private Permanent addReadySource() {
        Permanent source = new Permanent(new HonoredKnightCaptain());
        source.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(source);
        return source;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
