package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssembleFromParts.class, GrizzlyBears.class, LightningBolt.class})
class AssembleFromPartsTest extends BaseCardTest {

    @Test
    void perpetuallyGrantsAbilityAndCreatesZombieCopyThatShufflesSource() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new AssembleFromParts()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerDecks.get(player1.getId())).contains(bears);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    void onlyTargetsCreatureCardsInYourGraveyard() {
        Card assemble = new AssembleFromParts();
        harness.setHand(player1, List.of(assemble));
        harness.addMana(player1, ManaColor.BLACK, 1);
        Card noncreature = new LightningBolt();
        harness.setGraveyard(player1, List.of(noncreature));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
