package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BubbleSnare;
import com.github.laxika.magicalvibes.cards.c.CliffhavenKitesail;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.p.ProwlingFelidar;
import com.github.laxika.magicalvibes.cards.t.TrialOfZeal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LithoformEngine.class, CounselOfTheSoratami.class, GrizzlyBears.class, TrialOfZeal.class,
        BubbleSnare.class, CliffhavenKitesail.class, ProwlingFelidar.class, IntoTheRoil.class})
class LithoformEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a triggered ability you control")
    void copiesTriggeredAbility() {
        addReadyEngine();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        UUID triggerCardId = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();
        harness.activateAbility(player1, 0, 0, null, triggerCardId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Copies an instant or sorcery spell you control")
    void copiesInstantOrSorcerySpell() {
        addReadyEngine();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 1, null, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.SORCERY_SPELL).hasSize(2);
    }

    @Test
    @DisplayName("Copies a permanent spell as a token")
    void copiesPermanentSpellAsToken() {
        addReadyEngine();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.activateAbility(player1, 0, 2, null, bears.getId());
        resolveAllTriggers();

        List<Permanent> bearsOnBattlefield = findPermanents(player1, "Grizzly Bears");
        assertThat(bearsOnBattlefield).hasSize(2);
        assertThat(bearsOnBattlefield).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot copy a creature spell with the instant or sorcery ability")
    void cannotCopyCreatureSpellWithInstantOrSorceryAbility() {
        addReadyEngine();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copiedAuraRetainsItsTargetWithoutOfferingNewTargets() {
        addReadyEngine();
        Permanent creature = addCreatureReady(player2, new ProwlingFelidar());
        addCreatureReady(player2, new ProwlingFelidar());
        BubbleSnare snare = new BubbleSnare();
        harness.setHand(player1, List.of(snare));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.activateAbility(player1, 0, 2, null, snare.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Bubble Snare"))
                .hasSize(2)
                .allSatisfy(aura -> assertThat(aura.getAttachedTo()).isEqualTo(creature.getId()));
        assertThat(findPermanents(player1, "Bubble Snare"))
                .filteredOn(aura -> aura.getCard().isToken()).hasSize(1);
    }

    @Test
    void copiesActivatedAbilityAndAllowsANewTarget() {
        addReadyEngine();
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new CliffhavenKitesail());
        Permanent first = addCreatureReady(player1, new ProwlingFelidar());
        Permanent second = addCreatureReady(player1, new ProwlingFelidar());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, 0, null, first.getId());
        UUID equipId = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player1, 0, 0, null, equipId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(kitesail.getAttachedTo()).isEqualTo(second.getId());
        resolveAllTriggers();
        assertThat(kitesail.getAttachedTo()).isEqualTo(first.getId());
    }

    @Test
    void copiesInstantAndAllowsANewTarget() {
        addReadyEngine();
        Permanent first = addCreatureReady(player2, new ProwlingFelidar());
        Permanent second = addCreatureReady(player2, new ProwlingFelidar());
        IntoTheRoil roil = new IntoTheRoil();
        harness.setHand(player1, List.of(roil));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, first.getId());
        harness.activateAbility(player1, 0, 1, null, roil.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first, second);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(roil);
        assertThat(findPermanent(player1, "Lithoform Engine").isTapped()).isTrue();
    }

    @Test
    void cannotCopyOpponentsPermanentSpell() {
        addReadyEngine();
        ProwlingFelidar felidar = new ProwlingFelidar();
        harness.setHand(player2, List.of(felidar));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, felidar.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyEngine() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new LithoformEngine());
        engine.setSummoningSick(false);
    }
}
