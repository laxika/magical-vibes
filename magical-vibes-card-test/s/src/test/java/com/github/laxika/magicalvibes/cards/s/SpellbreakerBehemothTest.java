package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LordOfExtinction;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellbreakerBehemoth.class, AvatarOfMight.class, Cancel.class, GrizzlyBears.class,
        LordOfExtinction.class, Snakeform.class, Terminate.class})
class SpellbreakerBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Spellbreaker Behemoth's own spell can't be countered")
    void ownSpellCannotBeCountered() {
        SpellbreakerBehemoth behemoth = new SpellbreakerBehemoth();
        harness.setHand(player1, List.of(behemoth));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, behemoth.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellbreaker Behemoth");
        harness.assertNotInGraveyard(player1, "Spellbreaker Behemoth");
    }

    @Test
    @DisplayName("A power-5-or-greater creature spell you control can't be countered")
    void protectsHighPowerCreatureSpellsYouControl() {
        harness.addToBattlefield(player1, new SpellbreakerBehemoth());

        AvatarOfMight avatar = new AvatarOfMight();
        harness.setHand(player1, List.of(avatar));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, avatar.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avatar of Might");
        harness.assertNotInGraveyard(player1, "Avatar of Might");
    }

    @Test
    @DisplayName("A creature spell you control with power less than 5 can still be countered")
    void doesNotProtectLowPowerCreatureSpells() {
        harness.addToBattlefield(player1, new SpellbreakerBehemoth());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A high-power creature spell is not protected when its controller lacks the Behemoth")
    void doesNotProtectWhenBehemothIsOpponents() {
        // The Behemoth belongs to player2, so player1's own big creature spell stays counterable.
        harness.addToBattlefield(player2, new SpellbreakerBehemoth());

        AvatarOfMight avatar = new AvatarOfMight();
        harness.setHand(player1, List.of(avatar));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, avatar.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Avatar of Might");
        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Avatar of Might"));
    }

    @Test
    @DisplayName("Variable power is evaluated on the stack when determining counter protection")
    void protectsCreatureWithCharacteristicDefiningPower() {
        harness.addToBattlefield(player1, new SpellbreakerBehemoth());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        LordOfExtinction lord = new LordOfExtinction();
        harness.setHand(player1, List.of(lord));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, lord.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(lord.getId()));
        harness.assertNotInGraveyard(player1, "Lord of Extinction");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Lord of Extinction");
    }

    @Test
    @DisplayName("Losing its abilities removes the Behemoth's protection")
    void doesNotProtectAfterLosingAbilities() {
        harness.addToBattlefield(player1, new SpellbreakerBehemoth());
        AvatarOfMight avatar = new AvatarOfMight();
        harness.setHand(player1, List.of(avatar));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.setHand(player2, List.of(new Snakeform(), new Cancel()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Spellbreaker Behemoth"));
        harness.castAndResolveInstant(player2, 0, avatar.getId());

        harness.assertInGraveyard(player1, "Avatar of Might");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(avatar.getId()));
    }

    @Test
    @DisplayName("Removing the Behemoth in response makes a large creature spell counterable")
    void doesNotProtectAfterLeavingBattlefield() {
        harness.addToBattlefield(player1, new SpellbreakerBehemoth());
        AvatarOfMight avatar = new AvatarOfMight();
        harness.setHand(player1, List.of(avatar));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.setHand(player2, List.of(new Terminate(), new Cancel()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Spellbreaker Behemoth"));
        harness.assertInGraveyard(player1, "Spellbreaker Behemoth");
        harness.castAndResolveInstant(player2, 0, avatar.getId());

        harness.assertInGraveyard(player1, "Avatar of Might");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(avatar.getId()));
    }
}
