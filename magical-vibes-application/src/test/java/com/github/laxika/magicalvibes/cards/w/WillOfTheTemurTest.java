package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WillOfTheTemur.class, EdgarMarkov.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        SerraAngel.class})
class WillOfTheTemurTest extends BaseCardTest {

    @Test
    void createsModifiedDragonCopyOfTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castSingleMode(0, target.getId());

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    void targetPlayerDrawsGreatestControlledManaValue() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        castSingleMode(1, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);
    }

    @Test
    void commanderAllowsBothModes() {
        gd.playerCommandZones.get(player1.getId()).add(new EdgarMarkov());
        harness.addToBattlefield(player1, new EdgarMarkov());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        harness.setHand(player1, List.of(new WillOfTheTemur()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(copyTarget.getId(), player2.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.DRAGON));
    }

    @Test
    void cannotChooseBothModesWithoutCommander() {
        harness.setHand(player1, List.of(new WillOfTheTemur()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(player2.getId(), player2.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSingleMode(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new WillOfTheTemur()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{mode}, List.of(targetId), null);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
