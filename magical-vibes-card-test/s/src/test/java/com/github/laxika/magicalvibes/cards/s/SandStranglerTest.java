package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DesertOfTheFervent;
import com.github.laxika.magicalvibes.cards.k.KhenraEternal;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SandStrangler.class, DesertOfTheFervent.class, KhenraEternal.class})
class SandStranglerTest extends BaseCardTest {

    @Test
    @DisplayName("With a Desert on the battlefield, ETB may deal 3 damage to target creature")
    void etbDamagesWithDesertOnBattlefield() {
        harness.addToBattlefield(player1, new DesertOfTheFervent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());

        castSandStrangler();
        harness.passBothPriorities(); // resolve creature -> target prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve ETB -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Khenra Eternal");
    }

    @Test
    @DisplayName("With a Desert in the graveyard, ETB may deal 3 damage to target creature")
    void etbDamagesWithDesertInGraveyard() {
        harness.setGraveyard(player1, List.of(new DesertOfTheFervent()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());

        castSandStrangler();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Declining the may deals no damage")
    void decliningMayDealsNoDamage() {
        harness.addToBattlefield(player1, new DesertOfTheFervent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());

        castSandStrangler();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Without any Desert, the ETB does not trigger")
    void noDesertDoesNotTrigger() {
        harness.addToBattlefield(player2, new KhenraEternal());

        castSandStrangler();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Sand Strangler");
    }

    @Test
    @DisplayName("Losing the last Desert before resolution prevents damage and the may prompt")
    void losingLastDesertBeforeResolutionDoesNothing() {
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new DesertOfTheFervent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());

        castSandStrangler();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(desert);
        harness.setExile(player1, List.of(desert.getCard()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Khenra Eternal");
    }

    @Test
    @DisplayName("Removing the last graveyard Desert before resolution prevents damage")
    void losingGraveyardDesertBeforeResolutionDoesNothing() {
        DesertOfTheFervent desert = new DesertOfTheFervent();
        harness.setGraveyard(player1, List.of(desert));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());

        castSandStrangler();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(desert));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Khenra Eternal");
    }

    @Test
    @DisplayName("A Desert moving from battlefield to graveyard still satisfies the condition")
    void desertMovingToGraveyardStillAllowsDamage() {
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new DesertOfTheFervent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());

        castSandStrangler();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(desert);
        harness.setGraveyard(player1, List.of(desert.getCard()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Khenra Eternal");
        harness.assertNotOnBattlefield(player2, "Khenra Eternal");
    }

    @Test
    @DisplayName("Deserts controlled by the opponent or in their graveyard do not enable the trigger")
    void opponentsDesertsDoNotEnableTrigger() {
        harness.addToBattlefield(player2, new DesertOfTheFervent());
        harness.setGraveyard(player2, List.of(new DesertOfTheFervent()));
        harness.addToBattlefield(player2, new KhenraEternal());

        castSandStrangler();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Sand Strangler");
        harness.assertOnBattlefield(player2, "Khenra Eternal");
    }

    @Test
    @DisplayName("Sand Strangler may target itself when it is the only creature")
    void canTargetItself() {
        harness.addToBattlefield(player1, new DesertOfTheFervent());

        castSandStrangler();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Sand Strangler"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Sand Strangler");
        harness.assertNotOnBattlefield(player1, "Sand Strangler");
    }

    @Test
    @DisplayName("Removing Sand Strangler after triggering does not prevent its damage")
    void sourceLeavingDoesNotPreventDamage() {
        harness.addToBattlefield(player1, new DesertOfTheFervent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());

        castSandStrangler();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(harness.getPermanentId(player1, "Sand Strangler")))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.setExile(player1, List.of(source.getCard()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Khenra Eternal");
        harness.assertNotOnBattlefield(player2, "Khenra Eternal");
    }

    @Test
    @DisplayName("An absent target makes the trigger do nothing without prompting")
    void targetLeavingBeforeResolutionDoesNothing() {
        harness.addToBattlefield(player1, new DesertOfTheFervent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KhenraEternal());

        castSandStrangler();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setExile(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Sand Strangler");
    }

    private void castSandStrangler() {
        harness.castFromHand(player1, new SandStrangler(), "{3}{R}");
    }
}
