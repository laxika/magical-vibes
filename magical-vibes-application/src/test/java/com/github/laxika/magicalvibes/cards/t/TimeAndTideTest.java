package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Breezekeeper;
import com.github.laxika.magicalvibes.cards.d.DarkPrivilege;
import com.github.laxika.magicalvibes.cards.k.KatabaticWinds;
import com.github.laxika.magicalvibes.cards.m.MagmaMine;
import com.github.laxika.magicalvibes.cards.r.RainbowEfreet;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Breezekeeper.class, DarkPrivilege.class, KatabaticWinds.class, MagmaMine.class,
        RainbowEfreet.class, TimeAndTide.class, Warthog.class})
class TimeAndTideTest extends BaseCardTest {

    @Test
    @DisplayName("Phases out every creature with phasing")
    void phasesOutCreaturesWithPhasing() {
        Permanent keeper = addCreatureReady(player1, new Breezekeeper());
        Permanent opponentKeeper = addCreatureReady(player2, new Breezekeeper());

        castTimeAndTide();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(keeper);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(keeper);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentKeeper);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opponentKeeper);
    }

    @Test
    @DisplayName("Phases in every phased-out creature")
    void phasesInPhasedOutCreatures() {
        Permanent efreet = phaseOutWithRainbowEfreet();

        castTimeAndTide();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(efreet);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(efreet);
    }

    @Test
    @DisplayName("Phases in phased-out creatures controlled by the opponent")
    void phasesInOpponentsPhasedOutCreatures() {
        Permanent efreet = phaseOutWithRainbowEfreet(player2);

        castTimeAndTide();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(efreet);
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), List.of())).doesNotContain(efreet);
    }

    @Test
    @DisplayName("Simultaneously phases in phased-out creatures and phases out creatures with phasing")
    void simultaneousSwap() {
        Permanent efreet = phaseOutWithRainbowEfreet();
        Permanent keeper = addCreatureReady(player1, new Breezekeeper());

        castTimeAndTide();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(efreet);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(keeper);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(keeper);
    }

    @Test
    @DisplayName("A phased-out creature with phasing phases in and does not immediately phase out")
    void phasedOutPhasingCreaturePhasesInAndStays() {
        Permanent keeper = addCreatureReady(player1, new Breezekeeper());
        advanceToControllersUntap(player1);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(keeper);

        castTimeAndTide();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keeper);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(keeper);
    }

    @Test
    @DisplayName("Does not phase out creatures without phasing")
    void ignoresCreaturesWithoutPhasing() {
        Permanent warthog = addCreatureReady(player1, new Warthog());

        castTimeAndTide();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(warthog);
    }

    @Test
    @DisplayName("Does not phase out a noncreature permanent with phasing")
    void ignoresNoncreaturePermanentsWithPhasing() {
        Permanent winds = harness.addToBattlefieldAndReturn(player1, new KatabaticWinds());

        castTimeAndTide();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(winds);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(winds);
    }

    @Test
    @DisplayName("Phases attachments out with their creature and back in with it")
    void phasesAttachmentsWithTheirHost() {
        Permanent keeper = addCreatureReady(player1, new Breezekeeper());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DarkPrivilege());
        aura.setAttachedTo(keeper.getId());

        castTimeAndTide();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(keeper, aura);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(keeper, aura);

        castTimeAndTide();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keeper, aura);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of()))
                .doesNotContain(keeper, aura);
        assertThat(aura.getAttachedTo()).isEqualTo(keeper.getId());
    }

    @Test
    @DisplayName("Does not phase in phased-out noncreature permanents")
    void ignoresPhasedOutNoncreatures() {
        Permanent magmaMine = new Permanent(new MagmaMine());
        gd.phasedOutPermanents.computeIfAbsent(player1.getId(), id -> new ArrayList<>()).add(magmaMine);

        castTimeAndTide();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(magmaMine);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(magmaMine);
    }

    @Test
    @DisplayName("Phases in a face-down creature")
    void phasesInFaceDownCreature() {
        Permanent faceDownCreature = new Permanent(new MagmaMine());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        gd.phasedOutPermanents.computeIfAbsent(player1.getId(), id -> new ArrayList<>()).add(faceDownCreature);

        castTimeAndTide();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(faceDownCreature);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of()))
                .doesNotContain(faceDownCreature);
    }

    private void castTimeAndTide() {
        harness.castFromHand(player1, new TimeAndTide(), "{U}{U}");
        harness.passBothPriorities();
    }

    private Permanent phaseOutWithRainbowEfreet() {
        return phaseOutWithRainbowEfreet(player1);
    }

    private Permanent phaseOutWithRainbowEfreet(Player controller) {
        Permanent efreet = addCreatureReady(controller, new RainbowEfreet());
        harness.addMana(controller, ManaColor.BLUE, 2);
        int efreetIndex = gd.playerBattlefields.get(controller.getId()).indexOf(efreet);
        harness.activateAbility(controller, efreetIndex, null, null);
        harness.passBothPriorities();
        return efreet;
    }

    private void advanceToControllersUntap(Player controller) {
        harness.forceStep(TurnStep.CLEANUP);
        if (gd.activePlayerId.equals(controller.getId())) {
            harness.passBothPriorities();
            harness.forceStep(TurnStep.CLEANUP);
            harness.passBothPriorities();
        } else {
            harness.passBothPriorities();
        }
    }
}
