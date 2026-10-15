package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.o.Oakenform;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DruidOfHorns.class, CentaurCourser.class, Oakenform.class,
        Cancel.class, Murder.class})
class DruidOfHornsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Aura targeting Druid of Horns creates a 3/3 green Beast token")
    void auraTargetingDruidCreatesBeastToken() {
        UUID druidId = harness.addToBattlefieldAndReturn(player1, new DruidOfHorns()).getId();
        harness.setHand(player1, List.of(new Oakenform()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, druidId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting an Aura targeting another creature does not trigger Druid of Horns")
    void auraTargetingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new DruidOfHorns());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new CentaurCourser());
        harness.setHand(player1, List.of(new Oakenform()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, otherCreature.getId());

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .isEmpty();
    }

    @Test
    @DisplayName("The Beast is created before the Aura resolves")
    void triggerResolvesBeforeAura() {
        UUID druidId = harness.addToBattlefieldAndReturn(player1, new DruidOfHorns()).getId();
        harness.setHand(player1, List.of(new Oakenform()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, druidId);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Oakenform");
        Permanent beast = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beast.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Oakenform");
    }

    @Test
    @DisplayName("An opponent's Aura targeting the Druid does not trigger it")
    void opponentsAuraDoesNotTrigger() {
        UUID druidId = harness.addToBattlefieldAndReturn(player1, new DruidOfHorns()).getId();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Oakenform()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castEnchantment(player2, 0, druidId);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Beast");
        harness.assertNotOnBattlefield(player2, "Beast");
    }

    @Test
    @DisplayName("A non-Aura spell targeting the Druid does not trigger it")
    void nonAuraTargetingDruidDoesNotTrigger() {
        UUID druidId = harness.addToBattlefieldAndReturn(player1, new DruidOfHorns()).getId();
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, druidId);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Druid of Horns");
        harness.assertNotOnBattlefield(player1, "Beast");
    }

    @Test
    @DisplayName("Countering the Aura does not prevent the Beast trigger from resolving")
    void counteredAuraStillCreatesBeast() {
        UUID druidId = harness.addToBattlefieldAndReturn(player1, new DruidOfHorns()).getId();
        Oakenform aura = new Oakenform();
        harness.setHand(player1, List.of(aura));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, druidId);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Oakenform");
        harness.assertNotOnBattlefield(player1, "Beast");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Beast");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the Druid in response does not stop its Beast trigger")
    void removedDruidStillCreatesBeast() {
        UUID druidId = harness.addToBattlefieldAndReturn(player1, new DruidOfHorns()).getId();
        harness.setHand(player1, List.of(new Oakenform()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, druidId);
        harness.castInstant(player2, 0, druidId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Druid of Horns");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Beast");
        harness.assertNotOnBattlefield(player2, "Beast");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Oakenform");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the targeted Druid triggers, and each subsequent Aura triggers again")
    void multipleDruidsAndRepeatedAuras() {
        UUID druidId = harness.addToBattlefieldAndReturn(player1, new DruidOfHorns()).getId();
        harness.addToBattlefield(player1, new DruidOfHorns());
        harness.setHand(player1, List.of(new Oakenform(), new Oakenform()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castEnchantment(player1, 0, druidId);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, druidId);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }
}
