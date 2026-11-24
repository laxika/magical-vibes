package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.VotesFor;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayVoteAdditionalTimeEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "10")
@CardRegistration(set = "NCC", collectorNumber = "109")
public class TivitSellerOfSecrets extends Card {

    public static final String EVIDENCE = "Evidence";
    public static final String BRIBERY = "Bribery";

    public TivitSellerOfSecrets() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), councilsDilemma());
        addTrigger(Triggers.dealsCombatDamageToPlayer(), councilsDilemma());
        addStaticEffect(new MayVoteAdditionalTimeEffect());
    }

    /** Each investigate and each Treasure is its own event, one per vote. */
    private static CardEffect councilsDilemma() {
        return SequenceEffect.of(
                VoteEffect.startingWithYou(EVIDENCE, BRIBERY),
                new RepeatEffect(new VotesFor(EVIDENCE), CreateTokenEffect.ofClueToken(1)),
                new RepeatEffect(new VotesFor(BRIBERY), CreateTokenEffect.ofTreasureToken(1)));
    }
}
