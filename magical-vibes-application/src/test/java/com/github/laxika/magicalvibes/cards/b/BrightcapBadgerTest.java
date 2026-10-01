package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FungusFrolic;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrightcapBadger.class, FungusFrolic.class, GrizzlyBears.class})
class BrightcapBadgerTest extends BaseCardTest {

    @Test
    void fungusAndSaprolingsYouControlCanTapForGreenMana() {
        harness.addToBattlefield(player1, new BrightcapBadger());
        Permanent fungus = addCreatureReady(player1, creatureWithSubtype("Fungus", CardSubtype.FUNGUS));
        Permanent saproling = addCreatureReady(player1, creatureWithSubtype("Saproling", CardSubtype.SAPROLING));
        Permanent opponentSaproling = addCreatureReady(player2,
                creatureWithSubtype("Opponent Saproling", CardSubtype.SAPROLING));
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, fungus), 0, null, null);
        harness.activateAbility(player1, battlefieldIndex(player1, saproling), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(fungus.isTapped()).isTrue();
        assertThat(saproling.isTapped()).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, opponentSaproling)).isEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, bear)).isEmpty();
    }

    @Test
    void createsSaprolingAtYourEndStep() {
        harness.addToBattlefield(player1, new BrightcapBadger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getName()).isEqualTo("Saproling");
        assertThat(tokens.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(tokens.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
    }

    @Test
    void adventureCreatesTwoSaprolingsAndExilesTheCard() {
        BrightcapBadger card = new BrightcapBadger();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .map(Permanent::getCard)
                .toList())
                .hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getSubtypes()).containsExactly(CardSubtype.SAPROLING);
                    assertThat(token.getPower()).isEqualTo(1);
                    assertThat(token.getToughness()).isEqualTo(1);
                });
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private Card creatureWithSubtype(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtype));
        card.setToken(true);
        return card;
    }
}
