package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.VoteForCreatureThenDestroyMostVotedEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "121")
@CardRegistration(set = "PIP", collectorNumber = "649")
public class Vault11VotersDilemma extends Card {

    public Vault11VotersDilemma() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                new Sum(new PlayersInGame(), new Fixed(-1)),
                "Human Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new VoteForCreatureThenDestroyMostVotedEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_III, new VoteForCreatureThenDestroyMostVotedEffect());
    }
}
