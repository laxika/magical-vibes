package com.github.laxika.magicalvibes.model.effect;

/**
 * Guild Feud's upkeep ability: the target opponent reveals the top three cards of their library,
 * may put a creature card from among them onto the battlefield, then puts the rest into their
 * graveyard; the controller then does the same with the top three cards of their own library. If
 * two creatures were put onto the battlefield this way, those creatures fight each other.
 *
 * <p>A single cohesive effect rather than composed pieces: the two reveal stages are sequential
 * player decisions (the opponent chooses first) and the closing fight is conditional on both of
 * them putting a creature onto the battlefield, so the stages must share one carrier state.
 */
public record GuildFeudEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
