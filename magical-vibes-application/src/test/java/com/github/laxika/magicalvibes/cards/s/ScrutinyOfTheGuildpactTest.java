package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrutinyOfTheGuildpact.class, GrizzlyBears.class, WoollyThoctar.class})
class ScrutinyOfTheGuildpactTest extends BaseCardTest {

    @Test
    void boostsOnlyOwnMulticoloredCreatures() {
        harness.addToBattlefield(player1, new ScrutinyOfTheGuildpact());
        Permanent ownMulticolored = harness.addToBattlefieldAndReturn(player1, new WoollyThoctar());
        Permanent ownMonocolored = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingMulticolored = harness.addToBattlefieldAndReturn(player2, new WoollyThoctar());

        assertThat(gqs.getEffectivePower(gd, ownMulticolored)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownMulticolored)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownMonocolored)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownMonocolored)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingMulticolored)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposingMulticolored)).isEqualTo(4);
    }

    @Test
    void mayIncorporateCreatureAndCreateDetectiveWhenCast() {
        ScrutinyOfTheGuildpact scrutiny = new ScrutinyOfTheGuildpact();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(scrutiny, bears));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PerpetualPowerToughnessChoice.class)
                .validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent detective = findPermanent(player1, "Detective");
        assertThat(detective.getCard().getPower()).isEqualTo(2);
        assertThat(detective.getCard().getToughness()).isEqualTo(2);
        assertThat(detective.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(detective.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(detective.getCard().getSubtypes()).contains(CardSubtype.DETECTIVE);
        assertThat(detective.getCard().isToken()).isTrue();
    }

    @Test
    void mayDeclineIncorporation() {
        harness.setHand(player1, List.of(new ScrutinyOfTheGuildpact(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
