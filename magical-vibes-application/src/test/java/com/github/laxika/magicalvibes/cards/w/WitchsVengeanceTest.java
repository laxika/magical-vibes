package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitchsVengeance.class, AvatarOfMight.class, GrizzlyBears.class, YouthfulKnight.class})
class WitchsVengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures of the chosen type get -3/-3 on every battlefield")
    void weakensCreaturesOfChosenType() {
        Permanent ownAvatar = addReadyCreature(player1, new AvatarOfMight());
        Permanent opposingAvatar = addReadyCreature(player2, new AvatarOfMight());
        Permanent ownBear = addReadyCreature(player1, new GrizzlyBears());
        Permanent opposingBear = addReadyCreature(player2, new GrizzlyBears());

        castWitchsVengeance(player1);
        harness.handleListChoice(player1, "AVATAR");

        assertThat(gqs.getEffectivePower(gd, ownAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opposingAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposingAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("The -3/-3 wears off at end of turn")
    void modifierWearsOffAtEndOfTurn() {
        Permanent avatar = addReadyCreature(player2, new AvatarOfMight());

        castWitchsVengeance(player1);
        harness.handleListChoice(player1, "AVATAR");

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(8);
    }

    private void castWitchsVengeance(Player caster) {
        harness.castFromHand(caster, new WitchsVengeance(), "{1}{B}{B}");
        harness.passBothPriorities();
    }

    @ParameterizedTest
    @ValueSource(strings = {"HUMAN", "KNIGHT"})
    @DisplayName("Either creature subtype can be chosen to kill a Human Knight")
    void eitherSubtypeReducesToughnessBelowZero(String creatureType) {
        harness.addToBattlefield(player1, new YouthfulKnight());
        harness.addToBattlefield(player2, new YouthfulKnight());

        castWitchsVengeance(player1);
        harness.handleListChoice(player1, creatureType);

        harness.assertNotOnBattlefield(player1, "Youthful Knight");
        harness.assertNotOnBattlefield(player2, "Youthful Knight");
        harness.assertInGraveyard(player1, "Youthful Knight");
        harness.assertInGraveyard(player2, "Youthful Knight");
        harness.assertInGraveyard(player1, "Witch's Vengeance");
    }

    @Test
    @DisplayName("A creature type absent from the battlefield may be chosen")
    void absentTypeLeavesCreaturesUnaffected() {
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());

        castWitchsVengeance(player1);
        harness.handleListChoice(player1, "AVATAR");

        harness.assertOnBattlefield(player2, "Youthful Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Witch's Vengeance");
    }

    @Test
    @DisplayName("The spell resolves on an empty battlefield")
    void resolvesWithoutCreatures() {
        castWitchsVengeance(player1);
        harness.handleListChoice(player1, "KNIGHT");

        harness.assertInGraveyard(player1, "Witch's Vengeance");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not get -3/-3")
    void laterCreaturesAreNotAffected() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());

        castWitchsVengeance(player1);
        harness.handleListChoice(player1, "AVATAR");
        Permanent later = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(8);
    }

    @Test
    @DisplayName("Creatures entering before resolution are affected")
    void creaturesAreDeterminedAtResolution() {
        harness.castFromHand(player1, new WitchsVengeance(), "{1}{B}{B}");
        harness.addToBattlefield(player2, new YouthfulKnight());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "KNIGHT");

        harness.assertNotOnBattlefield(player2, "Youthful Knight");
        harness.assertInGraveyard(player2, "Youthful Knight");
    }

    @ParameterizedTest
    @ValueSource(strings = {"FOOD", "VEHICLE"})
    @DisplayName("Noncreature subtypes cannot be chosen")
    void rejectsNoncreatureSubtype(String subtype) {
        castWitchsVengeance(player1);

        assertThatThrownBy(() -> harness.handleListChoice(player1, subtype))
                .isInstanceOf(IllegalArgumentException.class);

        harness.handleListChoice(player1, "KNIGHT");
        harness.assertInGraveyard(player1, "Witch's Vengeance");
    }

    @Test
    @DisplayName("Successive resolutions make independent creature type choices")
    void eachSpellChoosesItsOwnType() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.addToBattlefield(player2, new YouthfulKnight());

        castWitchsVengeance(player1);
        harness.handleListChoice(player1, "AVATAR");
        castWitchsVengeance(player1);
        harness.handleListChoice(player1, "KNIGHT");

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(5);
        harness.assertNotOnBattlefield(player2, "Youthful Knight");
        harness.assertInGraveyard(player2, "Youthful Knight");
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
