package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtherswornAdjudicator.class, AngelicChorus.class, FountainOfYouth.class, GrizzlyBears.class})
class EtherswornAdjudicatorTest extends BaseCardTest {


    @Test
    @DisplayName("Destroy ability destroys target creature")
    void destroysTargetCreature() {
        addAdjudicatorReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        addDestroyMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroy ability destroys target enchantment")
    void destroysTargetEnchantment() {
        addAdjudicatorReady(player1);
        harness.addToBattlefield(player2, new AngelicChorus());
        addDestroyMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonenchantment permanent")
    void cannotTargetArtifact() {
        addAdjudicatorReady(player1);
        harness.addToBattlefield(player2, new FountainOfYouth());
        addDestroyMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Untap ability untaps Ethersworn Adjudicator")
    void untapAbilityUntapsSelf() {
        Permanent adjudicator = addAdjudicatorReady(player1);
        adjudicator.tap();
        assertThat(adjudicator.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(adjudicator.isTapped()).isFalse();
    }

    @Test
    void destroyAbilityPaysTapCostImmediately() {
        Permanent adjudicator = addAdjudicatorReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        addDestroyMana(player1);

        harness.activateAbility(player1, 0, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(adjudicator.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void destroyAbilityCannotBeActivatedWhileSummoningSick() {
        harness.addToBattlefield(player1, new EtherswornAdjudicator());
        harness.addToBattlefield(player2, new GrizzlyBears());
        addDestroyMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("summoning sickness");
    }

    @Test
    void destroyAbilityCanTargetItself() {
        Permanent adjudicator = addAdjudicatorReady(player1);
        addDestroyMana(player1);

        harness.activateAbility(player1, 0, 0, null, adjudicator.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ethersworn Adjudicator");
        harness.assertInGraveyard(player1, "Ethersworn Adjudicator");
    }

    @Test
    void untapAbilityWorksWhileSummoningSickAndOnlyUntapsItsSource() {
        Permanent adjudicator = harness.addToBattlefieldAndReturn(player1, new EtherswornAdjudicator());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new EtherswornAdjudicator());
        adjudicator.tap();
        other.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(adjudicator.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(adjudicator.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
    }

    private Permanent addAdjudicatorReady(Player player) {
        return addCreatureReady(player, new EtherswornAdjudicator());
    }

    private void addDestroyMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
