package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "219")
@CardRegistration(set = "DBL", collectorNumber = "219")
public class DireStrainRampage extends Card {

    /** "If a land was destroyed this way": the destruction happened and the target, as it last existed, was a land. */
    private static final Condition LAND_DESTROYED = new AllOf(List.of(
            HappenedThisWay.destroyed(), new TargetPermanentMatches(new PermanentIsLandPredicate())));

    public DireStrainRampage() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentIsLandPredicate()
                )),
                "Target must be an artifact, enchantment, or land"
        )).addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new ConditionalEffect(LAND_DESTROYED,
                        controllerMaySearch(2, "Search your library for up to two basic land cards?")))
                .addSpellEffect(new ConditionalEffect(new NotCondition(LAND_DESTROYED),
                        controllerMaySearch(1, "Search your library for a basic land card?")));
        addCastingOption(new FlashbackCast("{3}{R}{G}"));
    }

    /** "Its controller may search their library for up to N basic land cards, put them onto the battlefield tapped". */
    private static MayEffect controllerMaySearch(int count, String prompt) {
        return new MayEffect(new SearchLibraryEffect(new Fixed(count), CardPredicateUtils.basicLand(),
                LibrarySearchDestination.BATTLEFIELD_TAPPED).searchedBy(Subjects.controllerOf(Subjects.target())),
                prompt, null, MayChoicePlayer.TARGET_PERMANENT_CONTROLLER);
    }
}
