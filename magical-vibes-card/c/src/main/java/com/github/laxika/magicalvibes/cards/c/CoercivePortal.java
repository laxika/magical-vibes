package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VMA", collectorNumber = "266")
@CardRegistration(set = "LCC", collectorNumber = "109")
public class CoercivePortal extends Card {

    public static final String CARNAGE = "Carnage";
    public static final String HOMAGE = "Homage";

    public CoercivePortal() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                VoteEffect.startingWithYou(CARNAGE, HOMAGE),
                new ConditionalEffect(VoteResult.moreVotes(CARNAGE), SequenceEffect.of(
                        new SacrificeSelfEffect(),
                        DestroyTargetPermanentEffect.all(new PermanentNotPredicate(new PermanentIsLandPredicate())))),
                new ConditionalEffect(VoteResult.moreVotesOrTied(HOMAGE), new DrawCardEffect(1))));
    }
}
