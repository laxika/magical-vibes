package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.m.MotherOfRunes;
import com.github.laxika.magicalvibes.cards.r.Rancor;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Intervene.class, MightOfOaks.class, YavimayaWurm.class, LavaAxe.class, MotherOfRunes.class, Rancor.class})
class InterveneTest extends BaseCardTest {

    @Test
    void canTargetSpellThatTargetsCreature() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new YavimayaWurm());

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        Intervene intervene = new Intervene();
        harness.setHand(player2, List.of(intervene));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, wurm.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());

        assertThat(gd.stack).hasSize(2);
        StackEntry interveneEntry = gd.stack.getLast();
        assertThat(interveneEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(interveneEntry.getTargetId()).isEqualTo(might.getId());
    }

    @Test
    void cannotTargetCreaturePermanent() {
        YavimayaWurm wurm = new YavimayaWurm();
        harness.setHand(player1, List.of(wurm));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.setHand(player2, List.of(new Intervene()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, wurm.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersSpellThatTargetsCreature() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new YavimayaWurm());

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Intervene()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, wurm.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(might.getId()));
        harness.assertInGraveyard(player1, "Might of Oaks");
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Might of Oaks is countered."));
    }

    @Test
    void cannotTargetSpellThatTargetsPlayer() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new Intervene()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, lavaAxe.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fizzlesWhenTargetCreatureLeavesBattlefield() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new YavimayaWurm());

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Intervene()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, wurm.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());

        gd.playerBattlefields.get(player1.getId()).remove(wurm);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wurm);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Intervene") && entry.plainText().contains("fizzles"));
        harness.assertInGraveyard(player1, "Might of Oaks");
    }

    @Test
    void cannotTargetAbilityThatTargetsCreature() {
        Permanent motherOfRunes = addCreatureReady(player1, new MotherOfRunes());
        Permanent wurm = addCreatureReady(player1, new YavimayaWurm());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(motherOfRunes), null, wurm.getId());
        StackEntry abilityEntry = gd.stack.getLast();
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Intervene()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, abilityEntry.getTargetableId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersAuraSpellTargetingCreature() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new YavimayaWurm());
        Rancor rancor = new Rancor();
        harness.setHand(player1, List.of(rancor));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Intervene()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, wurm.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, rancor.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rancor");
        harness.assertNotInHand(player1, "Rancor");
        harness.assertNotOnBattlefield(player1, "Rancor");
    }

    @Test
    void canCounterOwnSpellTargetingOpponentsCreature() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new YavimayaWurm());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might, new Intervene()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, wurm.getId());
        harness.castAndResolveInstant(player1, 0, might.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Might of Oaks");
        harness.assertInGraveyard(player1, "Intervene");
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Might of Oaks is countered."));
    }

    @Test
    void stillCountersWhenCreatureGainsProtectionFromTargetedSpell() {
        addCreatureReady(player1, new MotherOfRunes());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new YavimayaWurm());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new Intervene()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, wurm.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, wurm.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.GREEN.name());
        assertThat(gqs.hasProtectionFrom(gd, wurm, CardColor.GREEN)).isTrue();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Might of Oaks");
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Might of Oaks is countered."));
    }
}
