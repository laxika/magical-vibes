package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthKingdomGeneral.class, BurstOfStrength.class, Forest.class})
class EarthKingdomGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by earthbending a land you control")
    void earthbendsLandOnEntry() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new EarthKingdomGeneral()));
        addMana(player1, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Declining the counter trigger leaves it available later that turn")
    void decliningDoesNotUseOncePerTurnTrigger() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new EarthKingdomGeneral());

        castBurstOfStrength(general);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertLife(player1, 20);

        castBurstOfStrength(general);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Accepting the counter trigger prevents another one that turn")
    void acceptingUsesOncePerTurnTrigger() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new EarthKingdomGeneral());

        castBurstOfStrength(general);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 21);

        castBurstOfStrength(general);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Cannot earthbend a land controlled by an opponent")
    void cannotTargetOpponentsLand() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new EarthKingdomGeneral()));
        addMana(player1, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownLand.getId())
                .doesNotContain(opponentLand.getId());
    }

    private void castBurstOfStrength(Permanent target) {
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player, int colorless) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, colorless);
    }
}
