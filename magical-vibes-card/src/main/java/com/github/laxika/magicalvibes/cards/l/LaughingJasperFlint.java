package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "215")
public class LaughingJasperFlint extends Card {

    private static final Set<CardSubtype> OUTLAW_SUBTYPES = Set.of(
            CardSubtype.ASSASSIN,
            CardSubtype.MERCENARY,
            CardSubtype.PIRATE,
            CardSubtype.ROGUE,
            CardSubtype.WARLOCK);

    public LaughingJasperFlint() {
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(
                CardSubtype.MERCENARY,
                GrantScope.ALL_OWN_CREATURES,
                false,
                new PermanentNotPredicate(new PermanentOwnedBySourceControllerPredicate())));

        PermanentCount outlawsYouControl = new PermanentCount(
                new PermanentHasAnySubtypePredicate(OUTLAW_SUBTYPES), CountScope.CONTROLLER);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Must target an opponent"))
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, new ExileTopCardsToSourceEffect(
                        outlawsYouControl, false, false, LibraryScope.TARGET_OPPONENT, true));

        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(
                true, null, false, false, 0, null, false, true, false));
    }
}
