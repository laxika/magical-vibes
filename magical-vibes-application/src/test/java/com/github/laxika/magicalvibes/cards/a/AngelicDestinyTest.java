package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicDestiny.class, DarkBanishing.class, GrizzlyBears.class, Spellbook.class})
class AngelicDestinyTest extends BaseCardTest {

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }

    private Permanent attachDestiny(Player controller, Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new AngelicDestiny());
        aura.setAttachedTo(enchanted.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +4/+4 and has flying and first strike")
    void grantsBoostAndKeywords() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachDestiny(player1, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature is an Angel in addition to its other types")
    void grantsAngelSubtype() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachDestiny(player1, bears);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bears);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.ANGEL);
    }

    @Test
    @DisplayName("Bonuses fall off when Angelic Destiny leaves the battlefield")
    void bonusesRemovedWhenAuraLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = attachDestiny(player1, bears);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("When the enchanted creature dies, Angelic Destiny returns to its owner's hand")
    void returnsToHandWhenEnchantedCreatureDies() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachDestiny(player1, bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        resolveStack();

        harness.assertNotOnBattlefield(player1, "Angelic Destiny");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Destiny"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Angelic Destiny"));
    }

    @Test
    @DisplayName("Angelic Destiny stays in the graveyard when it is destroyed on its own")
    void staysInGraveyardWhenAuraItselfIsDestroyed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = attachDestiny(player1, bears);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        resolveStack();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Destiny"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Angelic Destiny"));
    }

    @Test
    @DisplayName("Angelic Destiny cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new AngelicDestiny()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID spellbookId = harness.getPermanentId(player2, "Spellbook");
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, spellbookId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting Angelic Destiny attaches it to an opposing creature and grants its bonuses")
    void resolvesOnOpposingCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AngelicDestiny()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveStack();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Angelic Destiny"))
                .findFirst().orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.ANGEL)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.BEAR)).isTrue();
    }

    @Test
    @DisplayName("Death of an opposing enchanted creature returns the Aura to its owner")
    void returnsToAuraOwnerWhenOpposingCreatureDies() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachDestiny(player1, bears);
        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        resolveStack();

        harness.assertInHand(player1, "Angelic Destiny");
        harness.assertNotInHand(player2, "Angelic Destiny");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Angelic Destiny");
    }

    @Test
    @DisplayName("Angelic Destiny goes to the graveyard if its target dies before it resolves")
    void targetDiesBeforeAuraResolves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AngelicDestiny()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        resolveStack();

        harness.assertInGraveyard(player1, "Angelic Destiny");
        harness.assertNotInHand(player1, "Angelic Destiny");
        harness.assertNotOnBattlefield(player1, "Angelic Destiny");
    }

    @Test
    @DisplayName("Exiling the enchanted creature does not return Angelic Destiny to hand")
    void noReturnWhenEnchantedCreatureIsExiled() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachDestiny(player1, bears);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, bears));
        harness.runStateBasedActions();
        resolveStack();

        harness.assertInGraveyard(player1, "Angelic Destiny");
        harness.assertNotInHand(player1, "Angelic Destiny");
    }
}
