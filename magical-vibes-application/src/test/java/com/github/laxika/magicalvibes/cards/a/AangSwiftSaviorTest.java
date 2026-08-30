package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        AangSwiftSavior.class,
        AangAndLaOceansFury.class,
        GrizzlyBears.class,
        LightningStrike.class
})
class AangSwiftSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB airbends another creature and grants its owner a generic alternative cost")
    void airbendsAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AangSwiftSavior()));
        addAangMana();

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(target.getOriginalCard().getId())).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB can airbend a spell on the stack")
    void airbendsSpellOnStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        LightningStrike lightningStrike = new LightningStrike();
        AangSwiftSavior aangCard = new AangSwiftSavior();
        harness.setHand(player1, List.of(lightningStrike, aangCard));
        harness.addMana(player1, ManaColor.RED, 1);
        addAangMana();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(lightningStrike.getId()));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(lightningStrike.getId());

        harness.handlePermanentChosen(player1, lightningStrike.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(lightningStrike.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(lightningStrike.getId())).isEqualTo(player1.getId());
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(lightningStrike.getId())
                && entry.getEntryType() == StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Waterbend transforms Aang")
    void waterbendTransformsAang() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangSwiftSavior());
        addCreatureReady(player1, new GrizzlyBears(), 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(aang.isTransformed()).isTrue();
        assertThat(aang.getCard()).isInstanceOf(AangAndLaOceansFury.class);
    }

    @Test
    @DisplayName("Aang and La puts counters on each tapped creature you control when it attacks")
    void attackCountersTappedCreatures() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangSwiftSavior());
        aang.setCard(aang.getOriginalCard().getBackFaceCard());
        aang.setTransformed(true);
        aang.setSummoningSick(false);
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears(), 1);
        Permanent untappedCreature = addCreatureReady(player1, new GrizzlyBears(), 1);
        tappedCreature.tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(aang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tappedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(untappedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addAangMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addCreatureReady(Player player, com.github.laxika.magicalvibes.model.Card card,
                                       int count) {
        Permanent last = null;
        for (int i = 0; i < count; i++) {
            last = new Permanent(card instanceof GrizzlyBears ? new GrizzlyBears() : card);
            last.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(last);
        }
        return last;
    }
}
